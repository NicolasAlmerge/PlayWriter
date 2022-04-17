package playwriter;

import static playwriter.Utils.ARG_SEPARATOR;


/**
 * Represents a pair of arguments.
 */
public final class ArgumentPair implements Pair {
	private final String arg1;
	private final String arg2;
	private final int splitIndex;

	public ArgumentPair(String argument) {
		arg1 = argument.stripTrailing().toUpperCase();
		arg2 = null;
		splitIndex = -1;
	}
	
	public ArgumentPair(String argument, int index) {
		arg1 = argument.substring(0, index).stripTrailing().toUpperCase();
		arg2 = argument.substring(index+1).stripLeading();
		splitIndex = index;
	}

	@Override
	public String getFirstArgument() {
		return arg1;
	}

	@Override
	public String getSecondArgument() {
		return arg2;
	}
	
	@Override
	public int getSplitIndex() {
		return splitIndex;
	}
	
	/**
	 * @return Argument pair from line and 'ARG_SEPARATOR' separator.
	 */
	public static ArgumentPair getFrom(String line) {
		return getFrom(line, ARG_SEPARATOR);
	}
	
	/**
	 * @return Argument pair from line and separator.
	 */
	public static ArgumentPair getFrom(String line, char separator) {
		int index = line.indexOf(separator);
		if (index == -1) return new ArgumentPair(line);
		return new ArgumentPair(line, index);
	}
}
