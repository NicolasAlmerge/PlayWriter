package playwriter;

import java.util.List;

/**
 * Represents a play action.
 *
 * @author Nicolas Almerge
 * @since 1.0
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
