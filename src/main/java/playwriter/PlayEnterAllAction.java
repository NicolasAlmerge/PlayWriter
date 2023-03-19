package playwriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an action where all players should enter.
 *
 * @author Nicolas Almerge
 * @since 1.0
 */
public final class PlayEnterAllAction implements PlayAction {
  private final Play play;

  /**
   * Constructor.
   *
   * @param p Play for which the action is.
   */
  public PlayEnterAllAction(Play p) {
    play = p;
  }

  @Override
  public void execute(List<Character> characters) throws PlayCompileTimeError {
    List<String> names = new ArrayList<>();
    List<Character> remaining = play.getCharacters();

    for (Character c : characters) {
      Utils.check(
          !c.hasEntered(),
          "cannot exclude character '" + c.getName() + "' since it has already entered"
      );
      names.add(c.getName());
      remaining.remove(c);
    }

    for (Character c : remaining) {
      c.enter();
    }

    if (names.size() == 0) {
      play.writeStageDirections("ENTER ALL");
      return;
    }

    if (names.size() == 1) {
      play.writeStageDirections("ENTER ALL EXCEPT " + names.get(0));
      return;
    }

    String last = names.remove(names.size() - 1);
    play.writeStageDirections(
        "ENTER ALL EXCEPT " + String.join(", ", names)
        + " AND " + last
    );
  }
}