package playwriter;

import static playwriter.Utils.ARG_SEPARATOR;
import static playwriter.Utils.INDENTED_SPEECH_START;
import static playwriter.Utils.STAGE_DIR_START;
import static playwriter.Utils.SUBARGUMENT_START;
import static playwriter.Utils.check;
import static playwriter.Utils.failWith;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

/**
 * Represents a file parser.
 */
public final class FileParser {
  private static final Pattern WS_REGEX = Pattern.compile("\\s+");
  private final BufferedReader fp;
  private final FileInputStream wordFp;
  private final List<XWPFParagraph> paragraphs;
  private final Play play;
  private final LineParser lineParser = new LineParser();
  private int currParagraphIndex = 0;
  private CharacterView previousChar = null;
  private boolean newScene = false;

  /**
   * Represents a file parser.
   *
   * @param inputFileName  Input file name.
   * @param outputFileName Output file name.
   * @param isPlainText    <code>true</code> if the file is plain text, <code>false</code>
   *                       otherwise.
   * @throws IOException              if file reading failed.
   * @throws IllegalArgumentException if file is blank.
   */
  public FileParser(String inputFileName, String outputFileName, boolean isPlainText)
      throws IOException {
    if (isPlainText) {
      fp = new BufferedReader(new FileReader(inputFileName));
      wordFp = null;
      paragraphs = null;
    } else {
      fp = null;
      wordFp = new FileInputStream(inputFileName);
      paragraphs = new XWPFDocument(wordFp).getParagraphs();
    }
    play = new Play(outputFileName);
    Counter.reset();
    getNextLine();
    if (lineParser.consumed()) {
      throw new IllegalArgumentException("Error: input file is blank.");
    }
  }

  /**
   * Parses the whole file. The file is read line by line via a {@link BufferedReader} if the file
   * is a text file, and read from memory otherwise.
   *
   * @throws IOException          if a line reading failed.
   * @throws PlayCompileTimeError if option parsing failed.
   */
  public void parseAll() throws IOException, PlayCompileTimeError {
    parseHeaders();
    while (!lineParser.consumed()) {
      parseCurrentLine();
      getNextLine();
    }
  }

  /**
   * Closes and outputs the PDF file.
   *
   * @throws IOException if file closing failed.
   * @throws PlayCompileTimeError if play could not be closed because it is not finished.
   */
  public void output() throws IOException, PlayCompileTimeError {
    closeFile();
    play.outputPlay();
  }

  /**
   * Closes the file if possible, and writes a fail message on the output PDF.
   */
  public void closePlayWithFailMessage() {
    try {
      closeFile();
    } catch (IOException ignored) {
      // Do nothing if file could not be closed
    }

    // Write a fail message in the PDF output
    play.closePdfWithFailMessage();
  }

  /**
   * Reads the next line.
   *
   * @throws IOException if the line reading failed.
   */
  private void getNextLine() throws IOException {
    do {
      String line;
      if (fp != null) {
        line = fp.readLine();
      } else {
        line = (currParagraphIndex < paragraphs.size())
            ? paragraphs.get(currParagraphIndex++).getParagraphText() : null;
      }

      if (line == null) {
        lineParser.updateLine("");
        return;
      }

      Counter.increment();
      line = WS_REGEX.matcher(line.strip()).replaceAll(" ");
      if (!line.isEmpty()) {
        lineParser.updateLine(line);
        return;
      }
    } while (true);
  }

  /**
   * Closes the file.
   *
   * @throws IOException if file closing failed.
   */
  private void closeFile() throws IOException {
    if (fp != null) {
      fp.close();
    }
    if (wordFp != null) {
      wordFp.close();
    }
  }

  /**
   * Parses all the file's headers.
   *
   * @throws IOException          if a line reading failed.
   * @throws PlayCompileTimeError if option parsing failed.
   */
  private void parseHeaders() throws IOException, PlayCompileTimeError {
    boolean charsSet = false;
    boolean optionsSet = false;

    while (!lineParser.consumed()) {
      Pair pair = ArgumentPair.getFrom(lineParser.getLine());
      String name = pair.getFirstArgument();
      String value = pair.getSecondArgument();
      final String failMsg = "can only set 'AUTHOR:', 'TITLE:', 'CHARACTERS:' or 'OPTIONS:' before "
          + "the beginning of the play";

      if (name.equals("BEGIN") && value == null) {
        return;
      }

      check(value != null, failMsg);

      switch (name) {
        case "AUTHOR" -> {
          play.setAuthor(value);
          getNextLine();
        }
        case "TITLE" -> {
          play.setTitle(value);
          getNextLine();
        }
        case "CHARACTERS" -> {
          check(value.isEmpty(), "cannot set value on the same line for header 'CHARACTERS'");
          check(!charsSet, "cannot set multiple 'CHARACTERS' sections");
          charsSet = true;
          parseCharacters();
        }
        case "OPTIONS" -> {
          check(value.isEmpty(), "cannot set value on the same line for header 'OPTIONS'");
          check(!optionsSet, "cannot set multiple 'OPTIONS' sections");
          optionsSet = true;
          parseOptions();
        }
        default -> failWith(failMsg);
      }
    }
  }

  /**
   * Parses all the character names and descriptions.
   *
   * @throws IOException          if a line reading failed.
   * @throws PlayCompileTimeError if option parsing failed.
   */
  private void parseCharacters() throws IOException, PlayCompileTimeError {
    do {
      getNextLine();
      if (lineParser.consumed() || lineParser.getLine().charAt(0) != SUBARGUMENT_START) {
        break;
      }

      lineParser.updateLine(lineParser.getLine().substring(1));
      play.addCharacter(ArgumentPair.getFrom(lineParser.getLine()));
    } while (true);

    check(play.hasCharacters(), "no characters defined");
  }

  /**
   * Parses all the options.
   *
   * @throws IOException          if a line reading failed.
   * @throws PlayCompileTimeError if option parsing failed.
   */
  private void parseOptions() throws IOException, PlayCompileTimeError {
    Set<String> modified = new HashSet<>();
    do {
      getNextLine();
      if (lineParser.consumed() || lineParser.getLine().charAt(0) != SUBARGUMENT_START) {
        break;
      }

      lineParser.updateLine(lineParser.getLine().substring(1));
      String newLine = lineParser.getLine();
      check(!newLine.isEmpty(), "option line is empty");

      play.modifyOption(ArgumentPair.getFrom(newLine), modified);
    } while (true);
  }

  /**
   * Parses the current line.
   *
   * @throws PlayCompileTimeError if parsing failed.
   */
  private void parseCurrentLine() throws IOException, PlayCompileTimeError {
    String fullLine = lineParser.getLine();

    if (fullLine.charAt(0) == STAGE_DIR_START) {
      fullLine = fullLine.substring(1).stripLeading();
      play.writeStageDirections(fullLine);
      return;
    }

    String firstArgument = lineParser.getNextArgument().toUpperCase();

    if (firstArgument.equals("ONSTAGE")) {
      check(newScene, "ONSTAGE can only be used after a new scene");
      checkOneOrMoreArguments("ONSTAGE");
      newScene = false;
      play.parseAction(lineParser, new PlayOnStageAction(play), new PlayOnStageAllAction(play));
      return;
    }

    if (fullLine.equalsIgnoreCase("THE END")) {
      play.end();
      return;
    }

    switch (firstArgument) {
      case "BEGIN" -> {
        checkSingleArgument(firstArgument);
        play.begin();
        return;
      }
      case "CURTAIN" -> {
        checkSingleArgument(firstArgument);
        play.curtain();
        return;
      }
      case "NEWLINE" -> {
        checkSingleArgument(firstArgument);
        play.newLine();
        return;
      }
      case "NEWPAGE" -> {
        checkSingleArgument(firstArgument);
        play.newPage();
        return;
      }
      case "ACT" -> {
        checkOneOrMoreArguments(firstArgument);
        play.setAct(ArgumentPair.getFrom(lineParser.getLine()));
        return;
      }
      case "SCENE" -> {
        checkOneOrMoreArguments(firstArgument);
        previousChar = null;
        play.setScene(ArgumentPair.getFrom(lineParser.getLine()));
        newScene = true;
        return;
      }
      case "ENTER" -> {
        checkOneOrMoreArguments(firstArgument);
        play.parseAction(lineParser, new PlayEnterAction(play), new PlayEnterAllAction(play));
        newScene = false;
        previousChar = null;
        return;
      }
      case "EXIT" -> {
        checkOneOrMoreArguments(firstArgument);
        play.parseAction(lineParser, new PlayExitAction(play), new PlayExitAllAction(play));
        newScene = false;
        previousChar = null;
        return;
      }
      default -> {
      }
    }

    boolean offStage;

    if (firstArgument.equals("OFFSTAGE")) {
      checkOneOrMoreArguments("OFFSTAGE");
      fullLine = lineParser.getLine();
      offStage = true;
    } else {
      offStage = false;
    }

    Pair colon = ArgumentPair.getFrom(fullLine);
    Pair arrow = ArgumentPair.getFrom(fullLine, INDENTED_SPEECH_START);
    check(
        colon.getSecondArgument() != null || arrow.getSecondArgument() != null,
        "line must either contain a '" + ARG_SEPARATOR + "' or '" + INDENTED_SPEECH_START
            + "' character to denote a speech, or start by a '" + STAGE_DIR_START
            + "' character to denote a stage direction"
    );

    String charName;
    String content;
    CharacterView character;
    boolean whiteSpaces;

    if (arrow.getSecondArgument() == null) {
      charName = colon.getFirstArgument();
      content = colon.getSecondArgument();
      whiteSpaces = false;
    } else if (colon.getSecondArgument() == null || arrow.getSplitIndex() < colon.getSplitIndex()) {
      charName = arrow.getFirstArgument();
      content = arrow.getSecondArgument();
      whiteSpaces = true;
    } else {
      charName = colon.getFirstArgument();
      content = colon.getSecondArgument();
      whiteSpaces = false;
    }

    if (charName.isEmpty()) {
      check(previousChar != null, "cannot use '" + ARG_SEPARATOR
          + "' without anything before as the first line of a scene or after 'ENTER' or 'EXIT'"
      );
      character = previousChar;
      offStage = !character.hasEntered();
    } else {
      character = play.findCharacter(charName);
    }

    check(!content.isEmpty(), "cannot write empty speech for character " + character.getName());

    if (character == previousChar) {
      play.writeSpeech(character, content, offStage, false, whiteSpaces);
      return;
    }

    play.writeSpeech(character, content, offStage, true, whiteSpaces);
    newScene = false;
    previousChar = character;
  }

  /**
   * Asserts that the current line only contains one keyword (no arguments).
   *
   * @param keyword Keyword the line should start with.
   * @throws PlayCompileTimeError if assertion failed.
   */
  private void checkSingleArgument(String keyword) throws PlayCompileTimeError {
    check(
        lineParser.consumed(),
        String.format("'%s' keyword has to be alone on its line", keyword)
    );
  }

  /**
   * Asserts that the current line contains one or more arguments.
   *
   * @param keyword Keyword the line should start with.
   * @throws PlayCompileTimeError if assertion failed.
   */
  private void checkOneOrMoreArguments(String keyword) throws PlayCompileTimeError {
    check(
        !lineParser.consumed(),
        String.format("cannot use '%s' keyword without any characters", keyword)
    );
  }
}
