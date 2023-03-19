package playwriter;

/**
 * Represents a line parser.
 */
public final class LineParser {
  private String line = "";

  /**
   * Constructor.
   */
  public LineParser() {
  }

  /**
   * Updates the current line.
   *
   * @param newLine New line.
   */
  public void updateLine(String newLine) {
    line = newLine.strip();
  }

  /**
   * Gets the current line.
   *
   * @return Current line.
   */
  public String getLine() {
    return line;
  }

  /**
   * Checks whether the line has been fully consumed.
   *
   * @return <code>true</code> if the line has been fully consumed, <code>false</code> otherwise.
   */
  public boolean consumed() {
    return line.isEmpty();
  }

  /**
   * Gets the next argument, separated by {@link Utils#TOKEN_SEPARATOR}. <b>This modifies the
   * current line.</b>
   *
   * @return Next argument, or the whole line if no {@link Utils#TOKEN_SEPARATOR} found.
   */
  public String getNextArgument() {
    return getNextArgument(Utils.TOKEN_SEPARATOR);
  }

  /**
   * Gets the next argument, separated by <code>separator</code>. <b>This modifies the current
   * line.</b>
   *
   * @param separator Separator to use to split the string.
   * @return Next argument, or the whole line if no <code>separator</code> found.
   */
  public String getNextArgument(char separator) {
    int index = line.indexOf(separator);
    if (index == -1) {
      String res = line;
      line = "";
      return res;
    }

    String res = line.substring(0, index).stripTrailing();
    line = line.substring(index + 1).stripLeading();
    return res;
  }

  /**
   * Gets the first argument, separated by <code>separator</code>. <b>This does NOT modify the
   * current line.</b>
   *
   * @return First argument, or the whole line if no {@link Utils#TOKEN_SEPARATOR} found.
   */
  public String getFirstArgument() {
    return getFirstArgument(Utils.TOKEN_SEPARATOR);
  }

  /**
   * Gets the first argument, separated by {@link Utils#TOKEN_SEPARATOR}. <b>This does NOT modify
   * the current line.</b>
   *
   * @param separator Separator to use to split the current line.
   * @return First argument, or the whole line if no <code>separator</code> found.
   */
  public String getFirstArgument(char separator) {
    int index = line.indexOf(separator);
    if (index == -1) {
      return line;
    }

    return line.substring(0, index).stripTrailing();
  }
}
