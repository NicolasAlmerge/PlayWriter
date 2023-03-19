package playwriter;

import java.util.List;

/**
 * Represents a play action.
 */
public interface PlayAction {

  /**
   * Executes the action.
   *
   * @param characters {@link List} of {@link Character} to execute the action for.
   * @throws PlayCompileTimeError if a logical error happened.
   */
  void execute(List<Character> characters) throws PlayCompileTimeError;
}
