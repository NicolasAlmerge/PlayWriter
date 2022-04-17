package playwriter;

import static playwriter.Utils.check;

import java.util.ArrayList;
import java.util.List;

public final class PlayExitAllAction implements PlayAction {
	private final Play play;

	public PlayExitAllAction(Play pPlay) {
		play = pPlay;
	}

	@Override
	public void execute(List<Character> characters) {
		List<String> names = new ArrayList<>();
		List<Character> remaining = play.getCharacters();

		for (CharacterView c: characters) {
			check(c.hasEntered(), "cannot exclude character '" + c.getName() + "' since it has already exited");
			names.add(c.getName());
			remaining.remove(c);
		}

		for (Character c: remaining) c.exit();

		if (names.size() == 0) {
			play.writeStageDirections("EXIT ALL");
			return;
		}

		if (names.size() == 1) {
			play.writeStageDirections("EXIT ALL EXCEPT " + names.get(0));
			return;
		}
		
		final String LASTNAME = names.remove(names.size()-1);
		play.writeStageDirections("EXIT ALL EXCEPT " + String.join(", ", names) + " AND " + LASTNAME);
	}
}
