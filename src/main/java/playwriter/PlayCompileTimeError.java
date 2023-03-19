package playwriter;

/**
 * Represents a play-compile time error.
 *
 * @author Nicolas Almerge
 * @since 1.0
 */
public final class PlayCompileTimeError extends Exception {
  /**
   * Error message.
   */
  private final String message;

  /**
   * Constructor.
   *
   * @param errorMsg Error message.
   */
  public PlayCompileTimeError(String errorMsg) {
    message = "Error at line " + Counter.getLineNumber() + ": " + errorMsg + '.';
  }

  @Override
  public String getMessage() {
    return message;
  }
}
