package playwriter;

import static playwriter.Utils.check;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an action where all players should exit.
 */
public final class PlayExitAllAction implements PlayAction {
  private final Play play;

  /**
   * Constructor.
   *
   * @param p Play for which the action is.
   */
  public PlayExitAllAction(Play p) {
    play = p;
  }

  @Override
  public void execute(List<Character> characters) throws PlayCompileTimeError {
    List<String> names = new ArrayList<>();
    List<Character> remaining = play.getCharacters();

    for (Character c : characters) {
      check(c.hasEntered(),
          "cannot exclude character '" + c.getName() + "' since it has already exited");
      names.add(c.getName());
      remaining.remove(c);
    }

    for (Character c : remaining) {
      c.exit();
    }

    if (names.size() == 0) {
      play.writeStageDirections("EXIT ALL");
      return;
    }

    if (names.size() == 1) {
      play.writeStageDirections("EXIT ALL EXCEPT " + names.get(0));
      return;
    }

    final String lastName = names.remove(names.size() - 1);
    play.writeStageDirections(
        "EXIT ALL EXCEPT " + String.join(", ", names) + " AND "
        + lastName
    );
  }
}
