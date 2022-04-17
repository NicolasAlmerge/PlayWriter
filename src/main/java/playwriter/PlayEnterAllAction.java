package playwriter;

import static playwriter.Utils.check;

import java.util.ArrayList;
import java.util.List;


public final class PlayEnterAllAction implements PlayAction {
	private final Play play;

	public PlayEnterAllAction(Play pPlay) {
		play = pPlay;
	}

	@Override
	public void execute(List<Character> characters) {
		List<String> names = new ArrayList<>();
		List<Character> remaining = play.getCharacters();

		for (Character c: characters) {
			check(!c.hasEntered(), "cannot exclude character '" + c.getName() + "' since it has already entered");
			names.add(c.getName());
			remaining.remove(c);
		}

		for (Character c: remaining) c.enter();

		if (names.size() == 0) {
			play.writeStageDirections("ENTER ALL");
			return;
		}

		if (names.size() == 1) {
			play.writeStageDirections("ENTER ALL EXCEPT " + names.get(0));
			return;
		}

		String last = names.remove(names.size()-1);
		play.writeStageDirections("ENTER ALL EXCEPT " + String.join(", ", names) + " AND " + last);
	}
}