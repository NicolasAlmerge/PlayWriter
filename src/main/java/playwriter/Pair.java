package playwriter;

/**
 * Represents a pair of arguments.
 */
public interface Pair {

  /**
   * Gets the pair's first argument.
   *
   * @return Pair's first argument.
   */
  String getFirstArgument();

  /**
   * Gets the pair's second argument.
   *
   * @return Pair's second argument.
   */
  String getSecondArgument();

  /**
   * Gets the pair's split index, or <code>-1</code> if none is set.
   *
   * @return Pair's split index, or <code>-1</code> if none is set.
   */
  int getSplitIndex();
}
