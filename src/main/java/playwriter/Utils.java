package playwriter;

/**
 * Represents a collection of constants and utility functions.
 *
 * @author Nicolas Almerge
 * @since 1.0
 */
public final class Utils {

  /**
   * Application version.
   */
  public static final String VERSION = "1.0";

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
   * Array of all keywords.
   */
  private static final String[] KEYWORDS = {
      "ACT", "ASGROUP", "BEGIN", "CURTAIN", "END",
      "ENTER", "EXCEPT", "EXEUNT", "EXIT", "NEWLINE",
      "NEWPAGE", "OFFSTAGE", "ONSTAGE", "SCENE", stringOf(ARG_SEPARATOR),
      stringOf(VALUE_SEPARATOR), stringOf(STAGE_DIR_START), stringOf(INDENTED_SPEECH_START)
  };

  /**
   * Private constructor.
   */
  private Utils() {
  }

  /**
   * Check if word is a keyword.
   *
   * @param value {@link String} to check.
   * @return <code>true</code> if <code>value</code> is a keyword, <code>false</code> otherwise.
   */
  public static boolean isKeyword(String value) {
    for (String keyword : KEYWORDS) {
      if (value.equals(keyword)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Stops the program execution by throwing a {@link PlayCompileTimeError} with the given error
   * message.
   *
   * @param errorMsg Error message.
   * @throws PlayCompileTimeError with <code>errorMsg</code> as error message.
   */
  public static void failWith(String errorMsg) throws PlayCompileTimeError {
    throw new PlayCompileTimeError(errorMsg);
  }

  /**
   * Asserts a value is <code>true</code>, or throws {@link PlayCompileTimeError} if it is
   * <code>false</code>.
   *
   * @param result   Value to check.
   * @param errorMsg Error message to use in case the check failed.
   * @throws PlayCompileTimeError if <code>result</code> is <code>false</code>.
   */
  public static void check(boolean result, String errorMsg) throws PlayCompileTimeError {
    if (!result) {
      throw new PlayCompileTimeError(errorMsg);
    }
  }

  /**
   * Converts a {@link String} to an integer, between {@link Utils#MIN_FONT_SIZE} and
   * {@link Utils#MAX_FONT_SIZE}.
   *
   * @param numberText {@link String} to convert.
   * @return Integer conversion of text.
   * @throws PlayCompileTimeError if <code>numberText</code> doesn't have an unsigned integer
   *                              format, is less than {@link Utils#MIN_FONT_SIZE} or greater than
   *                              {@link Utils#MAX_FONT_SIZE}.
   */
  public static int convertFontToInt(String numberText) throws PlayCompileTimeError {
    return convertToInt(numberText, MIN_FONT_SIZE, MAX_FONT_SIZE);
  }

  /**
   * Converts a {@link String} to an integer, between <code>minimum</code> and <code>maximum</code>.
   *
   * @param numberText {@link String} to convert.
   * @param minimum    Minimum possible value of <code>numberText</code>.
   * @param maximum    Maximum possible value of <code>numberText</code>.
   * @return Integer conversion of text.
   * @throws PlayCompileTimeError if <code>numberText</code> doesn't have an unsigned integer
   *                              format, is less than <code>minimum</code> or greater than
   *                              <code>maximum</code>.
   */
  public static int convertToInt(String numberText, int minimum, int maximum)
      throws PlayCompileTimeError {
    int value;

    // Try to convert
    try {
      value = Integer.parseUnsignedInt(numberText);
    } catch (NumberFormatException e) {
      throw new PlayCompileTimeError(numberText + " is not recognised as a non-negative number");
    }

    // Check bounds
    check(
        value >= minimum && value <= maximum,
        "option value expect an integer from " + minimum + " to " + maximum
    );

    // Return value
    return value;
  }

  /**
   * Gets a {@link String} from a single character.
   *
   * @param c Character to convert.
   * @return {@link String} made of the single <code>c</code> character.
   */
  private static String stringOf(char c) {
    return String.valueOf(c);
  }
}
