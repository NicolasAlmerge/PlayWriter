package playwriter;


/**
 * Represents a pair of arguments.
 */
public interface Pair {

	/**
	 * @return Pair's first argument.
	 */
	String getFirstArgument();

	/**
	 * @return Pair's second argument.
	 */
	String getSecondArgument();
	
	/**
	 * @return Pair's split index.
	 */
	int getSplitIndex();
}
