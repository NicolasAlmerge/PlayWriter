package playwriter;

import static com.itextpdf.io.font.constants.StandardFonts.TIMES_BOLD;
import static com.itextpdf.io.font.constants.StandardFonts.TIMES_ITALIC;
import static com.itextpdf.io.font.constants.StandardFonts.TIMES_ROMAN;
import static com.itextpdf.kernel.font.PdfFontFactory.createFont;
import static com.itextpdf.layout.borders.Border.NO_BORDER;
import static com.itextpdf.layout.properties.TextAlignment.CENTER;
import static com.itextpdf.layout.properties.TextAlignment.JUSTIFIED;
import static com.itextpdf.layout.properties.TextAlignment.RIGHT;
import static com.itextpdf.layout.properties.VerticalAlignment.MIDDLE;
import static playwriter.Utils.ARG_SEPARATOR;
import static playwriter.Utils.INDENTED_SPEECH_START;
import static playwriter.Utils.MAX_PADDING_SIZE;
import static playwriter.Utils.MIN_PADDING_SIZE;
import static playwriter.Utils.VALUE_SEPARATOR;
import static playwriter.Utils.check;
import static playwriter.Utils.convertFontToInt;
import static playwriter.Utils.convertToInt;
import static playwriter.Utils.failWith;
import static playwriter.Utils.isKeyword;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a play.
 *
 * @author Nicolas Almerge
 * @since 1.0
 */
public final class Play {
  private static final String OFFSTAGE_TEXT = " (offstage)";
  private final Map<String, Character> characters = new HashMap<>();
  private final String fileName;
  private final PdfFont normalFont = createFont(TIMES_ROMAN);
  private final PdfFont boldFont = createFont(TIMES_BOLD);
  private final PdfFont italicFont = createFont(TIMES_ITALIC);
  private final Style normalFontStyle = new Style().setFont(normalFont);
  private final Style boldFontStyle = new Style().setFont(boldFont);
  private final Style italicFontStyle = new Style().setFont(italicFont);
  private final Rectangle pageSize;
  private String title = "";
  private String author = "";
  private boolean hasBegun = false;
  private boolean hasEnded = false;
  private boolean lastIsStageDir = false;
  private boolean outsideAct = true;
  private boolean outsideScene = true;
  private int actNumber = 0;
  private int sceneNumber = 0;
  private int textSize = 11;
  private int sceneSize = 13;
  private int actSize = 18;
  private int authorSize = 15;
  private int titleSize = 24;
  private int speechPadding = 6;
  private Document document;
  private float padding = 0;
  private float lastWidth = 0;
  private boolean hasTalked = false;

  /**
   * Constructor.
   *
   * @param outputFileName Output file name.
   * @throws IOException if file loading failed.
   */
  public Play(String outputFileName) throws IOException {
    fileName = outputFileName;
    PdfDocument pdf = new PdfDocument(new PdfWriter(new FileOutputStream(fileName)));
    pdf.getDocumentInfo().setCreator(Utils.getPdfContentCreator());
    pageSize = pdf.addNewPage().getPageSize();
    document = new Document(pdf);
  }

  /**
   * Gets the {@link List} of {@link Character} of the play.
   *
   * @return {@link List} of all {@link Character}.
   */
  public List<Character> getCharacters() {
    return new ArrayList<>(characters.values());
  }

  /**
   * Writes a new line.
   *
   * @throws IOException if writer is closed.
   */
  public void newLine() throws IOException {
    try (PdfWriter writer = getPdfDoc().getWriter()) {
      writer.writeNewLine();
    }
    lastIsStageDir = false;
  }

  /**
   * Adds a new page.
   */
  public void newPage() {
    document.add(new AreaBreak());
    lastIsStageDir = false;
  }

  /**
   * Sets the play title. This also writes it in the PDF metadata.
   *
   * @param newTitle New title.
   * @throws PlayCompileTimeError if play title is already set or <code>newTitle</code> is empty.
   */
  public void setTitle(String newTitle) throws PlayCompileTimeError {
    check(title.isEmpty(), "play title cannot be reset");
    check(!newTitle.isEmpty(), "play title is empty");
    title = newTitle;
    getPdfDoc().getDocumentInfo().setTitle(title);
  }

  /**
   * Sets the play author. This also writes it in the PDF metadata.
   *
   * @param newAuthor New title.
   * @throws PlayCompileTimeError if play author is already set or <code>newAuthor</code> is empty.
   */
  public void setAuthor(String newAuthor) throws PlayCompileTimeError {
    check(author.isEmpty(), "play author cannot be reset");
    check(!newAuthor.isEmpty(), "play author is empty");
    author = newAuthor;
    getPdfDoc().getDocumentInfo().setAuthor(author);
  }

  /**
   * Begins the play.
   *
   * @throws PlayCompileTimeError if play has ended, play has already begun, play has no title, no
   *                              author or no characters defined.
   */
  public void begin() throws PlayCompileTimeError {
    check(!hasEnded, "cannot begin a play that has ended");
    check(!hasBegun, "cannot use the 'BEGIN' keyword twice or more");
    check(!author.isEmpty() && !title.isEmpty(),
        "cannot begin a play with no title or author defined");
    check(hasCharacters(), "cannot begin a play with no characters defined");
    hasBegun = true;

    final float height = getPageHeight();
    Cell cell = new Cell();
    cell.add(new Paragraph(new Text(title).addStyle(boldFontStyle)).setFontSize(titleSize)
        .setTextAlignment(CENTER)).setMinHeight(height).setVerticalAlignment(MIDDLE);
    cell.add(new Paragraph(new Text(author).addStyle(normalFontStyle)).setFontSize(authorSize)
        .setTextAlignment(CENTER)).setMinHeight(height).setVerticalAlignment(MIDDLE);
    document.add(cell);

    for (String name : characters.keySet()) {
      float size = (float) Math.ceil(
          boldFont.getWidth(name, textSize) + italicFont.getWidth(OFFSTAGE_TEXT, textSize)
      );
      if (size > padding) {
        padding = size;
      }
    }
  }

  /**
   * Adds a character to the play based on a data {@link Pair}.
   *
   * @param pair Data {@link Pair}, representing a {@link Character}'s name and description.
   * @throws PlayCompileTimeError if play already begun, character's name is empty, character's name
   *                              is a special keyword, character's name is already taken, or
   *                              character's description is empty but specified.
   */
  public void addCharacter(Pair pair) throws PlayCompileTimeError {
    check(!hasBegun, "cannot add a character after the 'BEGIN' keyword");
    final String name = pair.getFirstArgument();
    final String description = pair.getSecondArgument();

    check(!name.isEmpty(), "character name not found");
    check(!isKeyword(name), "cannot define character '" + name + "' as it is a special keyword");
    check(characters.get(name) == null, "character '" + name + "' has already been defined");
    check(description == null || !description.isEmpty(),
        "character description empty (consider removing the '" + ARG_SEPARATOR
            + "' character if you don't want any description)"
    );

    characters.put(name, new Character(name, description));
  }

  /**
   * Checks whether the play has characters.
   *
   * @return <code>true</code> if the play has characters, <code>false</code> otherwise.
   */
  public boolean hasCharacters() {
    return !characters.isEmpty();
  }

  /**
   * Modifies an option. This function adds the option name to <code>modified</code>.
   *
   * @param pair     Data {@link Pair}
   * @param modified {@link Set} of modified options.
   * @throws PlayCompileTimeError if value not given, option name empty, option already set, option
   *                              value empty, or unknown option given.
   */
  public void modifyOption(Pair pair, Set<String> modified) throws PlayCompileTimeError {
    String name = pair.getFirstArgument();
    String value = pair.getSecondArgument();
    check(value != null, "'" + ARG_SEPARATOR + "' separator not found");

    value = value.toUpperCase();
    check(!name.isEmpty(), "option name cannot be empty");
    check(!modified.contains(name), "cannot set two or more values for option '" + name + "'");
    check(!value.isEmpty(), "option value cannot be empty");

    switch (name) {
      case "TEXT SIZE" -> textSize = convertFontToInt(value);
      case "SCENE SIZE" -> sceneSize = convertFontToInt(value);
      case "ACT SIZE" -> actSize = convertFontToInt(value);
      case "AUTHOR SIZE" -> authorSize = convertFontToInt(value);
      case "TITLE SIZE" -> titleSize = convertFontToInt(value);
      case "SPEECH PADDING" -> speechPadding = convertToInt(
          value, MIN_PADDING_SIZE, MAX_PADDING_SIZE
      );
      default -> failWith("unknown option name '" + name);
    }

    modified.add(name);
  }

  /**
   * Sets the new act for the play based on a data {@link Pair}.
   *
   * @param pair Data {@link Pair}.
   * @throws PlayCompileTimeError if not outside act, act number is now the successor of the
   *                              previous or act description is empty but not null.
   */
  public void setAct(Pair pair) throws PlayCompileTimeError {
    checkBetweenBeginAndEnd();
    check(outsideAct,
        "cannot start a new act since previous act was not closed ('CURTAIN' is missing)"
    );
    hasTalked = false;

    String numberText = pair.getFirstArgument();
    String description = pair.getSecondArgument();
    check(!numberText.isEmpty(), "act number cannot be empty");

    int value = convertFontToInt(numberText);
    check(actNumber + 1 == value,
        actNumber > 0 ? "cannot switch from act number " + actNumber + " to act number " + value :
            "first act must be number 1, not " + value);
    check(description == null || !description.isEmpty(),
        "act description cannot be empty (consider removing the '" + ARG_SEPARATOR
            + "' character if you don't want any description)");

    ++actNumber;
    sceneNumber = 0;
    if (actNumber > 1) {
      newPage();
    }

    Paragraph p = new Paragraph().setFontSize(actSize).setTextAlignment(CENTER);
    if (description == null) {
      p.add(new Text("ACT " + actNumber).addStyle(boldFontStyle));
    } else {
      p.add(
          new Text((actNumber == 1 ? "" : "\n") + "ACT " + actNumber + ": " + description).addStyle(
              boldFontStyle
          ));
    }

    document.add(p);
    outsideAct = false;
    lastWidth = 0;
    lastIsStageDir = false;
  }

  /**
   * Sets the new scene for the play based on a data {@link Pair}.
   *
   * @param pair Data {@link Pair}.
   * @throws PlayCompileTimeError if outside scene, no characters talked, outside act, scene number
   *                              empty, scene number is now the successor of the previous or
   *                              scene description is empty but not null.
   */
  public void setScene(Pair pair) throws PlayCompileTimeError {
    checkBetweenBeginAndEnd();
    check(outsideScene || hasTalked, "cannot end a scene where characters didn't talk");
    hasTalked = false;

    final String numberText = pair.getFirstArgument();
    final String description = pair.getSecondArgument();
    check(!outsideAct, "cannot define a new scene outside an act");
    check(!numberText.isEmpty(), "scene number cannot be empty");

    int value = convertFontToInt(numberText);
    check(sceneNumber + 1 == value, sceneNumber > 0
        ? "cannot switch from scene number " + sceneNumber + " to scene number " + value :
        "first scene of each act must be number 1, not " + value);
    check(description == null || !description.isEmpty(),
        "scene description cannot be empty (consider removing the '" + ARG_SEPARATOR
            + "' character if you don't want any description)");

    ++sceneNumber;

    Paragraph p = new Paragraph().setFontSize(sceneSize).setTextAlignment(CENTER);
    if (description == null) {
      p.add(new Text("SCENE " + sceneNumber).addStyle(boldFontStyle));
    } else {
      p.add(new Text(
          (sceneNumber == 1 ? "" : "\n") + "SCENE " + sceneNumber + ": " + description).addStyle(
          boldFontStyle
      ));
    }

    document.add(p);
    outsideScene = false;
    lastWidth = 0;
    lastIsStageDir = false;
  }

  /**
   * Parses action.
   *
   * @param lp          Line parser.
   * @param function    Function to execute with some characters.
   * @param functionAll Function to execute with all characters.
   * @throws PlayCompileTimeError if parsing failed.
   */
  public void parseAction(LineParser lp, PlayAction function, PlayAction functionAll)
      throws PlayCompileTimeError {
    checkInsideScene();
    String arg = lp.getFirstArgument().toUpperCase();
    boolean isAll;
    List<Character> inclusions = new ArrayList<>();

    if (arg.equals("ALL")) {
      isAll = true;
      lp.getNextArgument(); // Remove the "ALL" keyword

      // If nothing left, no exceptions
      if (lp.consumed()) {
        functionAll.execute(inclusions);
        return;
      }

      // Else we must have an "EXCEPT"
      arg = lp.getNextArgument().toUpperCase();
      check(arg.equals("EXCEPT"), "can only use 'EXCEPT' after the 'ALL' keyword");
    } else {
      isAll = false;
      check(!arg.equals("EXCEPT"), "'EXCEPT' can only be used after the 'ALL' keyword");
    }

    while (!lp.consumed()) {
      arg = lp.getNextArgument(VALUE_SEPARATOR).toUpperCase();
      check(!arg.isEmpty(),
          "cannot have blank spaces between '" + VALUE_SEPARATOR + "' characters");
      check(!arg.equals("ALL") && !arg.equals("EXCEPT"),
          "'" + arg + "' is a reserved keyword that cannot be interpreted as a play character");

      Character c = findCharacter(arg);
      check(!inclusions.contains(c), "character '" + c.getName() + "' mentionned twice or more");

      inclusions.add(c);
    }

    check(!inclusions.isEmpty(), isAll ? "cannot use 'ALL EXCEPT' without any character name" :
        "no characters set after the first keyword");
    if (isAll) {
      functionAll.execute(inclusions);
    } else {
      function.execute(inclusions);
    }
  }

  /**
   * Ends an act.
   *
   * @throws PlayCompileTimeError if no characters talked during the last act.
   */
  public void curtain() throws PlayCompileTimeError {
    checkInsideScene();
    check(hasTalked, "cannot end a scene where characters didn't talk");
    hasTalked = false;
    Paragraph p = new Paragraph().setFontSize(textSize);
    p.add(new Text("\n\0\t\tCURTAIN").addStyle(normalFontStyle));
    document.add(p);
    for (Character c : characters.values()) {
      c.forceExit();
    }
    outsideAct = true;
    outsideScene = true;
    lastWidth = 0;
    lastIsStageDir = false;
  }

  /**
   * Finds a character with name equal to <code>name</code>.
   *
   * @param name {@link Character}'s name.
   * @return Character with corresponding name.
   * @throws PlayCompileTimeError if character could not be found.
   */
  public Character findCharacter(String name) throws PlayCompileTimeError {
    Character c = characters.get(name);
    check(c != null, "unknown character '" + name + "'");
    return c;
  }

  /**
   * Writes a speech.
   *
   * @param c                     Character to write the speech for.
   * @param text                  Text to write.
   * @param offStage              Whether the character is offstage.
   * @param writeCharName         Whether to write the character's name before to the text.
   * @param hasLeadingWhitespaces Whether to include leading whitespaces.
   * @throws PlayCompileTimeError if <code>c</code> is on stage and <code>offStage</code>, or
   *                              <code>c</code> is not on stage and <code>!offStage</code>, or
   *                              incorrect use of <code>hasLeadingWhitespaces</code>.
   */
  public void writeSpeech(CharacterView c, String text, boolean offStage, boolean writeCharName,
                          boolean hasLeadingWhitespaces) throws PlayCompileTimeError {
    checkInsideScene();
    check(c.hasEntered() || offStage,
        String.format("cannot make character '%s' speak as is it not onstage", c.getName()));
    check(!c.hasEntered() || !offStage,
        String.format("cannot make character '%s' speak offstage as is it onstage", c.getName()));
    hasTalked = true;

    final float leftPadding = getPageWidth() - padding;
    Table table = new Table(new float[] {padding, leftPadding});
    Paragraph p = new Paragraph().setFontSize(textSize);
    if (writeCharName) {
      p.add(new Text(c.getName()).addStyle(boldFontStyle));
      if (offStage) {
        p.add(new Text(OFFSTAGE_TEXT).addStyle(italicFontStyle));
      }
    } else {
      check(!hasLeadingWhitespaces, "cannot use '" + INDENTED_SPEECH_START
          + "' with same play character as in the previous speech");
    }

    float newWidth = (normalFont.getWidth(text, textSize) + speechPadding) % leftPadding;
    Paragraph cont = new Paragraph(new Text(text).addStyle(normalFontStyle)).setFontSize(textSize);
    if (hasLeadingWhitespaces) {
      check(lastWidth > 0, "can only use '" + INDENTED_SPEECH_START + "' after another speech");
      newWidth += lastWidth;
      newWidth %= leftPadding;
      cont.setFirstLineIndent(lastWidth);
    }

    table.addCell(new Cell().setPaddingLeft(0).setPaddingRight(0).setBorder(NO_BORDER).add(p)
        .setTextAlignment(RIGHT));
    table.addCell(new Cell().setPaddingLeft(speechPadding).setBorder(NO_BORDER).add(cont)
        .setTextAlignment(JUSTIFIED));
    document.add(table);
    lastWidth = newWidth;
    lastIsStageDir = false;
  }

  /**
   * Writes stage directions.
   *
   * @param text Text to write.
   * @throws PlayCompileTimeError if tried to write them at an incorrect location.
   */
  public void writeStageDirections(String text) throws PlayCompileTimeError {
    checkBetweenBeginAndEnd();
    Paragraph p = new Paragraph().setFontSize(textSize);
    if (!lastIsStageDir) {
      p.setPaddingTop(textSize * (float) 0.75);
    }
    p.setPaddingBottom(textSize * (float) 0.75);
    p.add(new Text("\0\t\t" + text).addStyle(italicFontStyle));
    document.add(p);
    lastWidth = 0;
    lastIsStageDir = true;
  }

  /**
   * Ends the play.
   *
   * @throws PlayCompileTimeError if we could not properly end the play.
   */
  public void end() throws PlayCompileTimeError {
    check(outsideAct, "cannot end play without ending act (use the 'CURTAIN' keyword for that)");
    check(hasBegun, "cannot end a play that has not started");
    check(!hasEnded, "cannot use the 'END' keyword twice or more");

    hasEnded = true;
    lastIsStageDir = false;

    Paragraph p = new Paragraph().setFontSize(textSize).setTextAlignment(CENTER);
    p.add(new Text("\n\0\nTHE END").addStyle(boldFontStyle));
    document.add(p);
  }

  /**
   * Outputs the PDF file.
   *
   * @throws PlayCompileTimeError if play has not begun or ended.
   */
  public void outputPlay() throws PlayCompileTimeError {
    check(hasBegun, "cannot output a play that has not begun");
    check(hasEnded, "cannot output a play that has not ended");
    document.close();
  }

  /**
   * Closes the PDF with a fail message.
   */
  public void closePdfWithFailMessage() {
    document.close();
    try {
      PdfDocument pdf = new PdfDocument(new PdfWriter(new FileOutputStream(fileName)));
      pdf.addNewPage();
      pdf.getDocumentInfo().setCreator(Utils.getPdfContentCreator());
      document = new Document(pdf);
      final String error = "\n\n\n\n\n\n\n\nThe play generation failed due to a compilation error.";
      Paragraph p = new Paragraph(
          new Text(error).addStyle(new Style().setFont(createFont(TIMES_BOLD)))
      ).setFontSize(28).setTextAlignment(CENTER);
      document.add(p);
      document.close();
    } catch (IOException e) {
      // Ignore case where document closing failed
    }
  }

  /**
   * Resets the width to <code>0</code>.
   */
  public void resetWidth() {
    lastWidth = 0;
  }

  /**
   * Asserts we are between the beginning and end of the play.
   *
   * @throws PlayCompileTimeError if assertion failed.
   */
  private void checkBetweenBeginAndEnd() throws PlayCompileTimeError {
    check(hasBegun, "cannot write stage directions or dialog before the 'BEGIN' keyword");
    check(!hasEnded, "cannot write stage directions or dialog after the 'END' keyword");
  }

  /**
   * Asserts we are inside a scene.
   *
   * @throws PlayCompileTimeError if assertion failed.
   */
  private void checkInsideScene() throws PlayCompileTimeError {
    checkBetweenBeginAndEnd();
    check(!outsideScene, "cannot write stage directions or dialog outside acts or scenes");
  }

  /**
   * Gets the PDF document.
   *
   * @return PDF document.
   */
  private PdfDocument getPdfDoc() {
    return document.getPdfDocument();
  }

  /**
   * Computes the page width.
   *
   * @return Page width.
   */
  private float getPageWidth() {
    return pageSize.getWidth() - document.getLeftMargin() - document.getRightMargin();
  }

  /**
   * Computes the page height.
   *
   * @return Page height.
   */
  private float getPageHeight() {
    return pageSize.getHeight() - document.getTopMargin() - document.getBottomMargin();
  }
}
