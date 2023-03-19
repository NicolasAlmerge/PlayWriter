package playwriter;

/**
 * Represents a counter.
 */
public final class Counter {
  private static int lineNumber = 0;

  /**
   * Private constructor.
   */
  private Counter() {
  }

  /**
   * Gets the current line number.
   *
   * @return Current line number.
   */
  public static int getLineNumber() {
    return lineNumber;
  }

  /**
   * Increments the line number. This has no effect if the line number is equal to
   * {@link Integer#MAX_VALUE} to avoid overflowing.
   */
  public static void increment() {
    if (lineNumber < Integer.MAX_VALUE) {
      ++lineNumber;
    }
  }

  /**
   * Resets the line number to <code>0</code>.
   */
  public static void reset() {
    lineNumber = 0;
  }
}
