package playwriter;

import java.util.List;

/**
 * Represents an action where all players should be on stage at the beginning of the play.
 *
 * @author Nicolas Almerge
 * @since 1.0
 */
public final class PlayOnStageAllAction implements PlayAction {
  private final Play play;

  /**
   * Constructor.
   *
   * @param p Play for which the action is.
   */
  public PlayOnStageAllAction(Play p) {
    play = p;
  }

  @Override
  public void execute(List<Character> characters) throws PlayCompileTimeError {
    List<Character> remaining = play.getCharacters();

    for (Character c : characters) {
      Utils.check(
          !c.hasEntered(),
          "cannot exclude character '" + c.getName() + "' since it has already entered"
      );
      remaining.remove(c);
    }

    for (Character c : remaining) {
      c.enter();
    }

    play.resetWidth();
  }
}
