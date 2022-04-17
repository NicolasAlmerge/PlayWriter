package playwriter;

import java.util.ArrayList;
import java.util.List;


public final class PlayEnterAction implements PlayAction {
	private final Play play;

	public PlayEnterAction(Play pPlay) {
		play = pPlay;
	}

	@Override
	public void execute(List<Character> characters) {
		List<String> names = new ArrayList<>();

		for (Character c: characters) {
			c.enter();
			names.add(c.getName());
		}

		if (names.size() == 1) {
			play.writeStageDirections("ENTER " + names.get(0));
			return;
		}

		String last = names.remove(names.size()-1);
		play.writeStageDirections("ENTER " + String.join(", ", names) + " AND " + last);
	}
}
