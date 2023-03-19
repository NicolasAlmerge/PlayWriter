package playwriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an action where some players should exit.
 */
public final class PlayExitAction implements PlayAction {
  private final Play play;

  /**
   * Constructor.
   *
   * @param p Play for which the action is.
   */
  public PlayExitAction(Play p) {
    play = p;
  }

  @Override
  public void execute(List<Character> characters) throws PlayCompileTimeError {
    List<String> names = new ArrayList<>();

    for (Character c : characters) {
      c.exit();
      names.add(c.getName());
    }

    if (names.size() == 1) {
      play.writeStageDirections("EXIT " + names.get(0));
      return;
    }

    final String lastName = names.remove(names.size() - 1);
    play.writeStageDirections("EXIT " + String.join(", ", names) + " AND " + lastName);
  }
}
