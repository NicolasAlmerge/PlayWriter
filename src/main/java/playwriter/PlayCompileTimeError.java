package playwriter;

import static playwriter.Counter.getLineNumber;


/**
 * Represents a play-compile time error.
 */
public final class PlayCompileTimeError extends RuntimeException {
	private final String message;
	private static final long serialVersionUID = 1L;

	public PlayCompileTimeError(String errorMsg) {
        message = "Error at line " + getLineNumber() + ": " + errorMsg + '.';
    }

	@Override
    public String getMessage() {
        return message;
    }
}
