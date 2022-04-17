package playwriter;


import static playwriter.Utils.TOKEN_SEPARATOR;


/**
 * Represents a line parser.
 */
public final class LineParser {
	private String line = "";

	public void updateLine(String newLine) {
		line = newLine.strip();
	}

	public String getLine() {
		return line;
	}

	public boolean consumed() {
		return line.isEmpty();
	}
	
	/**
	 * @return Next argument with default token separator. This does modify string.
	 */
	public String getNextArgument() {
		return getNextArgument(TOKEN_SEPARATOR);
	}
	
	/**
	 * @return Next argument. This does modify string.
	 */
	public String getNextArgument(char separator) {
		int index = line.indexOf(separator);
		if (index == -1) {
			String res = line;
			line = "";
			return res;
		}

		String res = line.substring(0, index).stripTrailing();
		line = line.substring(index+1).stripLeading();
		return res;
	}
	
	/**
	 * @return First argument with default token separator. This does not modify string.
	 */
	public String getFirstArgument() {
		return getFirstArgument(TOKEN_SEPARATOR);
	}
	
	/**
	 * @return First argument. This does not modify string.
	 */
	public String getFirstArgument(char separator) {
		int index = line.indexOf(separator);
		if (index == -1) {
			return line;
		}

		return line.substring(0, index).stripTrailing();
	}
}
