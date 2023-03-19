package playwriter;

/**
 * Represents a pair of arguments.
 *
 * @author Nicolas Almerge
 * @since 1.0
 */
public final class ArgumentPair implements Pair {
  private final String arg1;
  private final String arg2;
  private final int splitIndex;

  /**
   * Constructor with only one {@link String} argument (second is <code>null</code>).
   *
   * @param argument Unique {@link String} argument.
   */
  public ArgumentPair(String argument) {
    arg1 = argument.stripTrailing().toUpperCase();
    arg2 = null;
    splitIndex = -1;
  }

  /**
   * Constructor with one {@link String} argument and the index at which to split it.
   *
   * @param argument {@link String} to split.
   * @param index    Split index.
   */
  public ArgumentPair(String argument, int index) {
    arg1 = argument.substring(0, index).stripTrailing().toUpperCase();
    arg2 = argument.substring(index + 1).stripLeading();
    splitIndex = index;
  }

  /**
   * Gets an {@link ArgumentPair} from a line, separated by {@link Utils#ARG_SEPARATOR}.
   *
   * @param line Text line.
   * @return {@link ArgumentPair} from line and {@link Utils#ARG_SEPARATOR} separator.
   */
  public static ArgumentPair getFrom(String line) {
    return getFrom(line, Utils.ARG_SEPARATOR);
  }

  /**
   * Gets an {@link ArgumentPair} from a line and character separator.
   *
   * @param line      Text line.
   * @param separator Character separator.
   * @return {@link ArgumentPair} from line and separator.
   */
  public static ArgumentPair getFrom(String line, char separator) {
    int index = line.indexOf(separator);
    if (index == -1) {
      return new ArgumentPair(line);
    }
    return new ArgumentPair(line, index);
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
}
