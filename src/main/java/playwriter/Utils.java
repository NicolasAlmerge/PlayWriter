package playwriter;

import static java.lang.Integer.parseUnsignedInt;


/**
 * Represents a collection of constants.
 */
public final class Utils {
	
	/**
	 * Start of a stage direction.
	 */
	public static final char STAGE_DIR_START = '*';
	
	/**
	 * Token separator.
	 */
	public static final char TOKEN_SEPARATOR = ' ';
	
	/**
	 * Argument separator.
	 */
	public static final char ARG_SEPARATOR = ':';

	/**
	 * Value separator.
	 */
	public static final char VALUE_SEPARATOR = ',';

	/**
	 * Start of a line with whitespace.
	 */
	public static final char INDENTED_SPEECH_START = '>';

	/**
	 * Start of a sub-argument.
	 */
	public static final char SUBARGUMENT_START = '-';

	/**
	 * Minimum font size.
	 */
	public static final int MIN_FONT_SIZE = 1;

	/**
	 * Maximum font size.
	 */
	public static final int MAX_FONT_SIZE = 48;
	
	/**
	 * Minimum padding size.
	 */
	public static final int MIN_PADDING_SIZE = 3;

	/**
	 * Maximum padding size.
	 */
	public static final int MAX_PADDING_SIZE = 50;

	/**
	 * Check if word is a keyword.
	 */
	public static boolean isKeyword(String value) {
		for (String keyword: KEYWORDS) if (value.equals(keyword)) return true;
		return false;
	}

	/**
	 * Performs a check, and throws an error if check was not successful.
	 */
    public static void check(boolean result, String errorMsg) {
    	if (!result) throw new PlayCompileTimeError(errorMsg);
    }
    
    /**
     * @return Integer conversion of text, if possible (between MIN_FONT_SIZE and MAX_FONT_SIZE).
     */
    public static int convertToInt(String numberText) {
    	return convertToInt(numberText, MIN_FONT_SIZE, MAX_FONT_SIZE);
    }
    
    /**
     * @return Integer conversion of text, if possible (between specified minimum and maximum).
     */
    public static int convertToInt(String numberText, int minimum, int maximum) {
    	int value;
    	try {
    		value = parseUnsignedInt(numberText);
    	} catch (NumberFormatException e) {
    		throw new PlayCompileTimeError(numberText + " is not recognised as a non-negative number");
    	}
    	check(value >= minimum && value <= maximum, "option value expect an integer from " + minimum + " to " + maximum);
    	return value;
    }
    
    private static String stringOf(char c) {
    	return String.valueOf(c);
    }
    
	private static final String[] KEYWORDS = {
		"ACT", "ASGROUP", "BEGIN", "CURTAIN", "END",
		"ENTER", "EXCEPT", "EXEUNT", "EXIT", "NEWLINE",
		"NEWPAGE", "OFFSTAGE", "ONSTAGE", "SCENE", stringOf(ARG_SEPARATOR),
		stringOf(VALUE_SEPARATOR), stringOf(STAGE_DIR_START), stringOf(INDENTED_SPEECH_START)
	};
    
	private Utils() {}
}
