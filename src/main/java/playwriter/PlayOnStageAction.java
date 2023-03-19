package playwriter;

import java.util.List;

/**
 * Represents an action where some players should be on stage at the beginning of the play.
 */
public final class PlayOnStageAction implements PlayAction {
  private final Play play;

  /**
   * Constructor.
   *
   * @param p Play for which the action is.
   */
  public PlayOnStageAction(Play p) {
    play = p;
  }

  @Override
  public void execute(List<Character> characters) throws PlayCompileTimeError {
    for (Character c : characters) {
      c.enter();
    }
    play.resetWidth();
  }
}
