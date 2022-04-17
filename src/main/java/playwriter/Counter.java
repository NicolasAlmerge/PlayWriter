package playwriter;


/**
 * Represents a counter.
 */
public final class Counter {
	private static int lineNumber = 0;
	
	/**
	 * @return Current line number.
	 */
	public static int getLineNumber() {
		return lineNumber;
	}
	
	/**
	 * Increments line number.
	 */
	public static void increment() {
		if (lineNumber < Integer.MAX_VALUE) ++lineNumber;
	}
	
	/**
	 * Resets line number.
	 */
	public static void reset() {
		lineNumber = 0;
	}
	
	private Counter() {}
}
