package playwriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an action where some players should enter.
 */
public final class PlayEnterAction implements PlayAction {
  private final Play play;

  /**
   * Constructor.
   *
   * @param p Play for which the action is.
   */
  public PlayEnterAction(Play p) {
    play = p;
  }

  @Override
  public void execute(List<Character> characters) throws PlayCompileTimeError {
    List<String> names = new ArrayList<>();

    for (Character c : characters) {
      c.enter();
      names.add(c.getName());
    }

    if (names.size() == 1) {
      play.writeStageDirections("ENTER " + names.get(0));
      return;
    }

    String last = names.remove(names.size() - 1);
    play.writeStageDirections("ENTER " + String.join(", ", names) + " AND " + last);
  }
}
