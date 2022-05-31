package playwriter;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import static com.itextpdf.layout.borders.Border.NO_BORDER;
import static com.itextpdf.layout.property.TextAlignment.CENTER;
import static com.itextpdf.layout.property.TextAlignment.RIGHT;
import static com.itextpdf.layout.property.TextAlignment.JUSTIFIED;
import static com.itextpdf.layout.property.VerticalAlignment.MIDDLE;
import static com.itextpdf.io.font.constants.StandardFonts.TIMES_BOLD;
import static com.itextpdf.io.font.constants.StandardFonts.TIMES_ITALIC;
import static com.itextpdf.io.font.constants.StandardFonts.TIMES_ROMAN;
import static com.itextpdf.kernel.font.PdfFontFactory.createFont;

import static playwriter.Utils.check;
import static playwriter.Utils.isKeyword;
import static playwriter.Utils.convertToInt;
import static playwriter.Utils.ARG_SEPARATOR;
import static playwriter.Utils.VALUE_SEPARATOR;
import static playwriter.Utils.MIN_PADDING_SIZE;
import static playwriter.Utils.MAX_PADDING_SIZE;
import static playwriter.Utils.INDENTED_SPEECH_START;


/**
 * Represents a play.
 */
public final class Play {
	private static final String OFFSTAGE_TEXT = " (offstage)";
	
	private String title = "";
    private String author = "";

    private boolean hasBegun = false;
    private boolean hasEnded = false;
    private boolean outsideAct = true;
    private boolean outsideScene = true;

    private final Map<String, Character> characters = new HashMap<>();

    private int actNumber = 0;
    private int sceneNumber = 0;

    private int textSize = 11;
    private int sceneSize = 13;
    private int actSize = 18;
    private int authorSize = 15;
    private int titleSize = 24;
    private int speechPadding = 6;
    
    private final String fileName;
    private Document document;
    
    private final PdfFont NORMAL = createFont(TIMES_ROMAN);
    private final PdfFont BOLD = createFont(TIMES_BOLD);
    private final PdfFont ITALIC = createFont(TIMES_ITALIC);
    private final Style DEFAULT_FONT = new Style().setFont(NORMAL);
    private final Style BOLD_FONT = new Style().setFont(BOLD);
    private final Style ITALIC_FONT = new Style().setFont(ITALIC);
    
    private float padding = 0;
    private float lastWidth = 0;
    private final Rectangle pageSize;
    private boolean hasTalked = false;
    private boolean newScene = false;
    
    public Play(String outputFileName) throws IOException {
    	fileName = outputFileName;
		PdfDocument pdf = new PdfDocument(new PdfWriter(new FileOutputStream(fileName)));
		pdf.addNewPage();
		pdf.getDocumentInfo().setAuthor("PlayWriter Application");
		document = new Document(pdf);
		pageSize = getPdfDoc().getPage(1).getPageSize();
    }
    
    public List<Character> getCharacters() {
    	return new ArrayList<>(characters.values());
    }
    
    public void newLine() {
    	getPdfDoc().getWriter().writeNewLine();
    }
    
    public void newPage() {
    	document.add(new AreaBreak());
    }

    public void setTitle(String newTitle) {
    	check(title.isEmpty(), "play title cannot be reset");
    	check(!newTitle.isEmpty(), "play title is empty");
        title = newTitle;
    }

    public void setAuthor(String newAuthor) {
    	check(author.isEmpty(), "play author cannot be reset");
    	check(!newAuthor.isEmpty(), "play author is empty");
        author = newAuthor;
    }
    
    public void begin() {
    	check(!hasEnded, "cannot begin a play that has ended");
    	check(!hasBegun, "cannot use the 'BEGIN' keyword twice or more");
    	check(!author.isEmpty() && !title.isEmpty(), "cannot begin a play with no title or author defined");
    	check(hasCharacters(), "cannot begin a play with no characters defined");
    	hasBegun = true;
    	
    	final float HEIGHT = getPageHeight();
    	Cell cell = new Cell();
    	cell.add(new Paragraph(new Text(title).addStyle(BOLD_FONT)).setFontSize(titleSize).setTextAlignment(CENTER)).setMinHeight(HEIGHT).setVerticalAlignment(MIDDLE);
    	cell.add(new Paragraph(new Text(author).addStyle(DEFAULT_FONT)).setFontSize(authorSize).setTextAlignment(CENTER)).setMinHeight(HEIGHT).setVerticalAlignment(MIDDLE);
    	document.add(cell);
    	
    	for (String name: characters.keySet()) {
    		float size = (float) Math.ceil(BOLD.getWidth(name, textSize) + ITALIC.getWidth(OFFSTAGE_TEXT, textSize));
    		if (size > padding) padding = size;
    	}
    }

    public void addCharacter(Pair pair) {
    	check(!hasBegun, "cannot add a character after the 'BEGIN' keyword");
    	String name = pair.getFirstArgument();
    	String description = pair.getSecondArgument();

    	check(!name.isEmpty(), "character name not found");
    	check(!isKeyword(name), "cannot define character '" + name + "' as it is a special keyword");
    	check(characters.get(name) == null, "character '" + name + "' has already been defined");
    	check(description == null || !description.isEmpty(), "character description empty (consider removing the '" + ARG_SEPARATOR + "' character if you don't want any description)");

    	characters.put(name, new Character(name, description));
    }

    /**
     * @return True if play has characters, false otherwise.
     */
    public boolean hasCharacters() {
    	return characters.size() > 0;
    }

    /**
     * Modifies an option.
     * This function adds the option name to the set of modified options.
     */
    public void modifyOption(Pair pair, Set<String> modified) {
    	String name = pair.getFirstArgument();
    	String value = pair.getSecondArgument();
    	check(value != null, "'" + ARG_SEPARATOR + "' separator not found");

        value = value.toUpperCase();
        check(!name.isEmpty(), "option name cannot be empty");
        check(!modified.contains(name), "cannot set two or more values for option '" + name + "'");
        check(!value.isEmpty(), "option value cannot be empty");

        switch (name) {
        	case "TEXT SIZE":
        		textSize = convertToInt(value);
        		break;
        	case "SCENE SIZE":
        		sceneSize = convertToInt(value);
        		break;
        	case "ACT SIZE":
        		actSize = convertToInt(value);
        		break;
        	case "AUTHOR SIZE":
        		authorSize = convertToInt(value);
        		break;
        	case "TITLE SIZE":
        		titleSize = convertToInt(value);
        		break;
        	case "SPEECH PADDING":
        		speechPadding = convertToInt(value, MIN_PADDING_SIZE, MAX_PADDING_SIZE);
        		break;
        	default:
        		check(false, "unknown option name '" + name);
        }

        modified.add(name);
    }

    public void setAct(Pair pair) {
    	checkBetweenBeginAndEnd();
    	check(outsideScene || hasTalked, "cannot end a scene where characters didn't talk");
    	hasTalked = false;
    	
    	String numberText = pair.getFirstArgument();
    	String description = pair.getSecondArgument();
    	check(!numberText.isEmpty(), "act number cannot be empty");

    	int value = convertToInt(numberText);
    	check(actNumber+1 == value, actNumber > 0? "cannot switch from act number " + actNumber + " to act number " + value: "first act must be number 1, not " + value);
    	check(description == null || !description.isEmpty(), "act description cannot be empty (consider removing the '" + ARG_SEPARATOR + "' character if you don't want any description)");

    	++actNumber;
    	sceneNumber = 0;
    	if (actNumber > 1) newPage();

    	Paragraph p = new Paragraph().setFontSize(actSize).setTextAlignment(CENTER);
    	if (description == null) p.add(new Text("ACT " + actNumber).addStyle(BOLD_FONT));
    	else p.add(new Text((actNumber == 1? "": "\n") + "ACT " + actNumber + ": " + description).addStyle(BOLD_FONT));

    	document.add(p);
    	outsideAct = false;
    	lastWidth = 0;
    }

    public void setScene(Pair pair) {
    	checkBetweenBeginAndEnd();
    	check(outsideScene || hasTalked, "cannot end a scene where characters didn't talk");
    	hasTalked = false;
    	newScene = true;

    	String numberText = pair.getFirstArgument();
    	String description = pair.getSecondArgument();
    	check(!outsideAct, "cannot define a new scene outside an act");
    	check(!numberText.isEmpty(), "scene number cannot be empty");

    	int value = convertToInt(numberText);
    	check(sceneNumber+1 == value, sceneNumber > 0? "cannot switch from scene number " + sceneNumber + " to scene number " + value: "first scene of each act must be number 1, not " + value);
    	check(description == null || !description.isEmpty(), "scene description cannot be empty (consider removing the '" + ARG_SEPARATOR + "' character if you don't want any description)");

    	++sceneNumber;

    	Paragraph p = new Paragraph().setFontSize(sceneSize).setTextAlignment(CENTER);
    	if (description == null) p.add(new Text("SCENE " + sceneNumber).addStyle(BOLD_FONT));
    	else p.add(new Text((sceneNumber == 1? "": "\n") + "SCENE " + sceneNumber + ": " + description).addStyle(BOLD_FONT));

    	document.add(p);
    	outsideScene = false;
    	lastWidth = 0;
    }

    public void parseAction(LineParser lp, PlayAction function, PlayAction functionAll) {
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
    		check(!arg.isEmpty(), "cannot have blank spaces between '" + VALUE_SEPARATOR + "' characters");
    		check(!arg.equals("ALL") && !arg.equals("EXCEPT"), "'" + arg + "' is a reserved keyword that cannot be interpreted as a play character");

	    	Character c = findCharacter(arg);
	    	check(!inclusions.contains(c), "character '" + c.getName() + "' mentionned twice or more");

	    	inclusions.add(c);
    	}

    	check(!inclusions.isEmpty(), isAll? "cannot use 'ALL EXCEPT' without any character name": "no characters set after the first keyword");
    	if (isAll) functionAll.execute(inclusions);
    	else function.execute(inclusions);
    }

    public void curtain() {
    	checkInsideScene();
    	check(hasTalked, "cannot end a scene where characters didn't talk");
    	hasTalked = false;
    	Paragraph p = new Paragraph().setFontSize(textSize);
    	p.add(new Text("\nCURTAIN").addStyle(DEFAULT_FONT));
    	document.add(p);
    	outsideAct = true;
    	outsideScene = true;
    	lastWidth = 0;
    }

    public Character findCharacter(String name) {
        Character c = characters.get(name);
        check(c != null, "unknown character '" + name + "'");
        return c;
    }
    
    public void writeSpeech(CharacterView c, String text, boolean offStage, boolean writeCharName, boolean hasLeadingWhitespaces) {
    	checkInsideScene();
    	check(c.hasEntered() || offStage, String.format("cannot make character '%s' speak as is it not onstage", c.getName()));
    	check(!c.hasEntered() || !offStage, String.format("cannot make character '%s' speak offstage as is it onstage", c.getName()));
    	hasTalked = true;
    	
    	final float LEFTPADDING = getPageWidth()-padding;
    	Table table = new Table(new float[] {padding, LEFTPADDING});
    	Paragraph p = new Paragraph().setFontSize(textSize);
    	if (writeCharName) {
    		p.add(new Text(c.getName()).addStyle(BOLD_FONT));
    		if (offStage) p.add(new Text(OFFSTAGE_TEXT).addStyle(ITALIC_FONT));
    	} else check(!hasLeadingWhitespaces, "cannot use '" + INDENTED_SPEECH_START + "' with same play character as in the previous speech");
    	
    	float newWidth = (NORMAL.getWidth(text, textSize) + speechPadding) % LEFTPADDING;
    	Paragraph content = new Paragraph(new Text(text).addStyle(DEFAULT_FONT)).setFontSize(textSize);
    	if (hasLeadingWhitespaces) {
    		check(lastWidth > 0, "can only use '" + INDENTED_SPEECH_START + "' after another speech");
    		newWidth += lastWidth;
    		newWidth %= LEFTPADDING;
    		content.setFirstLineIndent(lastWidth);
    	}
    	
    	table.addCell(new Cell().setPaddingLeft(0).setPaddingRight(0).setBorder(NO_BORDER).add(p).setTextAlignment(RIGHT));
    	table.addCell(new Cell().setPaddingLeft(speechPadding).setBorder(NO_BORDER).add(content).setTextAlignment(JUSTIFIED));
    	document.add(table);
    	lastWidth = newWidth;
    	newScene = false;
    }
    
    public void writeStageDirections(String text, boolean addNewSpaces) {
    	checkBetweenBeginAndEnd();
    	Paragraph p = new Paragraph().setFontSize(textSize);
    	if (addNewSpaces) p.add(new Text(((newScene || lastWidth > 0)? "\n": "") + text + "\n\0\n").addStyle(ITALIC_FONT));
    	else p.add(new Text(((newScene || lastWidth > 0)? "\n": "") + "\0\t\t" + text + "\n\0\n").addStyle(ITALIC_FONT));
    	document.add(p);
    	lastWidth = 0;
    	newScene = false;
    }
    
    public void writeStageDirections(String text) {
    	writeStageDirections(text, true);
    }
    
    public void end() {
    	check(outsideAct, "cannot end play without ending act (use the 'CURTAIN' keyword for that)");
    	check(hasBegun, "cannot end a play that has not started");
    	check(!hasEnded, "cannot use the 'END' keyword twice or more");

        hasEnded = true;
        
        Paragraph p = new Paragraph().setFontSize(textSize).setTextAlignment(CENTER);
        p.add(new Text("\n\0\nTHE END").addStyle(BOLD_FONT));
        document.add(p);
    }
    
    public void outputPlay() {
    	check(hasBegun, "cannot output a play that has not begun");
    	check(hasEnded, "cannot output a play that has not ended");
    	document.close();
    }
    
    public void closePdfWithFailMessage() {
    	document.close();
		try {
			PdfDocument pdf = new PdfDocument(new PdfWriter(new FileOutputStream(fileName)));
			pdf.addNewPage();
			pdf.getDocumentInfo().setAuthor("PlayWriter Application");
			document = new Document(pdf);
	    	Paragraph p = new Paragraph(new Text("\n\n\n\n\n\n\n\nThe play generation failed due to a compilation error.").addStyle(new Style().setFont(createFont(TIMES_BOLD)))).setFontSize(28).setTextAlignment(CENTER);
	    	document.add(p);
	    	document.close();
		} catch (IOException e) {}
    }
    
    public void resetWidth() {
    	lastWidth = 0;
    }

    private void checkBetweenBeginAndEnd() {
    	check(hasBegun, "cannot write stage directions or dialog before the 'BEGIN' keyword");
    	check(!hasEnded, "cannot write stage directions or dialog after the 'END' keyword");
    }

    private void checkInsideScene() {
    	checkBetweenBeginAndEnd();
    	check(!outsideScene, "cannot write stage directions or dialog outside acts or scenes");
    }
    
    private PdfDocument getPdfDoc() {
    	return document.getPdfDocument();
    }
    
    private float getPageWidth() {
    	return pageSize.getWidth() - document.getLeftMargin() - document.getRightMargin();
    }
    
    private float getPageHeight() {
    	return pageSize.getHeight() - document.getTopMargin() - document.getBottomMargin();
    }
}
