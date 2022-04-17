package playwriter;


import static playwriter.Utils.check;
import static playwriter.Utils.ARG_SEPARATOR;
import static playwriter.Utils.SUBARGUMENT_START;
import static playwriter.Utils.STAGE_DIR_START;
import static playwriter.Utils.INDENTED_SPEECH_START;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Represents a file parser.
 */
public final class FileParser {
	private final BufferedReader fp;
	private final Play play;
	private CharacterView previousChar = null;
	private boolean newScene = false;
	private final LineParser lineParser = new LineParser();
	private static final Pattern WS_REGEX = Pattern.compile("\\s+");

	public FileParser(FileReader fileReader, String outputFileName) throws IOException {
		fp = new BufferedReader(fileReader);
		play = new Play(outputFileName);
		Counter.reset();
		getNextLine();
		if (lineParser.consumed()) throw new IllegalArgumentException("Error: input file is blank.");
	}

	public void parseAll() throws IOException {
		parseHeaders();
		while (!lineParser.consumed()) {
			parseCurrentLine();
			getNextLine();
		}
	}

	public void output() {
		play.outputPlay();
	}

	private void getNextLine() throws IOException {
		do {
			String line = fp.readLine();
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

	private void parseHeaders() throws IOException {
		boolean charsSet = false;
		boolean optionsSet = false;

		while (!lineParser.consumed()) {
			Pair pair = ArgumentPair.getFrom(lineParser.getLine());
			String name = pair.getFirstArgument();
			String value = pair.getSecondArgument();

			if (name.equals("BEGIN") && value == null) return;

			check(value != null, "can only set 'AUTHOR:', 'TITLE:', 'CHARACTERS:' or 'OPTIONS:' before the beginning of the play");

			switch (name) {
				case "AUTHOR":
					play.setAuthor(value);
					getNextLine();
					break;
				case "TITLE":
					play.setTitle(value);
					getNextLine();
					break;
				case "CHARACTERS":
					check(value.isEmpty(), "cannot set value on the same line for header 'CHARACTERS'");
					check(!charsSet, "cannot set multiple 'CHARACTERS' sections");
	                charsSet = true;
	                parseCharacters();
	                break;
				case "OPTIONS":
					check(value.isEmpty(), "cannot set value on the same line for header 'OPTIONS'");
					check(!optionsSet, "cannot set multiple 'OPTIONS' sections");
	                optionsSet = true;
	                parseOptions();
	                break;
	            default:
	            	check(false, "can only set 'AUTHOR:', 'TITLE:', 'CHARACTERS:' or 'OPTIONS:' before the beginning of the play");
			}
		}
	}

	private void parseCharacters() throws IOException {
		do {
            getNextLine();
            if (lineParser.consumed() || lineParser.getLine().charAt(0) != SUBARGUMENT_START) break;
            
            lineParser.updateLine(lineParser.getLine().substring(1));
            play.addCharacter(ArgumentPair.getFrom(lineParser.getLine()));
		} while (true);
		
		check(play.hasCharacters(), "no characters defined");
	}

	private void parseOptions() throws IOException {
		Set<String> modified = new HashSet<>();
		do {
            getNextLine();
            if (lineParser.consumed() || lineParser.getLine().charAt(0) != SUBARGUMENT_START) break;
            
            lineParser.updateLine(lineParser.getLine().substring(1));
            String newLine = lineParser.getLine();
            check(!newLine.isEmpty(), "option line is empty");
            
            play.modifyOption(ArgumentPair.getFrom(newLine), modified);
		} while (true);
	}

	private void parseCurrentLine() {
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
			play.parseAction(lineParser, new PlayOnStageAction(), new PlayOnStageAllAction(play));
			return;
		}
		
		if (fullLine.toUpperCase().equals("THE END")) {
			play.end();
			return;
		}

		switch (firstArgument) {
			case "BEGIN":
				checkSingleArgument(firstArgument);
				play.begin();
				return;
			case "CURTAIN":
				checkSingleArgument(firstArgument);
				play.curtain();
				return;
			case "NEWLINE":
				checkSingleArgument(firstArgument);
				play.newLine();
				return;
			case "NEWPAGE":
				checkSingleArgument(firstArgument);
				play.newPage();
				return;
			case "ACT":
				checkOneOrMoreArguments(firstArgument);
				play.setAct(ArgumentPair.getFrom(lineParser.getLine()));
				return;
			case "SCENE":
				checkOneOrMoreArguments(firstArgument);
				previousChar = null;
				play.setScene(ArgumentPair.getFrom(lineParser.getLine()));
				newScene = true;
				return;
			case "ENTER":
				checkOneOrMoreArguments(firstArgument);
				play.parseAction(lineParser, new PlayEnterAction(play), new PlayEnterAllAction(play));
				newScene = false;
				previousChar = null;
				return;
			case "EXIT":
				checkOneOrMoreArguments(firstArgument);
				play.parseAction(lineParser, new PlayExitAction(play), new PlayExitAllAction(play));
				newScene = false;
				previousChar = null;
				return;
		}

		boolean offStage;
		
		if (firstArgument.equals("OFFSTAGE")) {
			checkOneOrMoreArguments("OFFSTAGE");
			fullLine = lineParser.getLine();
			offStage = true;
		} else offStage = false;
		
		Pair colon = ArgumentPair.getFrom(fullLine);
		Pair arrow = ArgumentPair.getFrom(fullLine, INDENTED_SPEECH_START);
		check(colon.getSecondArgument() != null || arrow.getSecondArgument() != null, "line must either contain a '" + ARG_SEPARATOR + "' or '" + INDENTED_SPEECH_START + "' character to denote a speech, or start by a '" + STAGE_DIR_START + "' character to denote a stage direction");
		
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
			check(previousChar != null, "cannot use '" + ARG_SEPARATOR + "' without anything before as the first line of a scene or after 'ENTER' or 'EXIT'");
			character = previousChar;
			offStage = !character.hasEntered();
		} else character = play.findCharacter(charName);
		
		check(!content.isEmpty(), "cannot write empty speech for character " + character.getName());
		
		if (character == previousChar) {
			play.writeSpeech(character, content, offStage, false, whiteSpaces);
			return;
		}

		play.writeSpeech(character, content, offStage, true, whiteSpaces);
		newScene = false;
		previousChar = character;
	}
	
	private void checkSingleArgument(String keyword) {
		check(lineParser.consumed(), String.format("'%s' keyword has to be alone on its line", keyword));
	}

	private void checkOneOrMoreArguments(String keyword) {
		check(!lineParser.consumed(), String.format("cannot use '%s' keyword without any characters", keyword));
	}
}
