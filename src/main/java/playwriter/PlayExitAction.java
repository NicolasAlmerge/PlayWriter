package playwriter;

import java.util.ArrayList;
import java.util.List;


public final class PlayExitAction implements PlayAction {
	private final Play play;

	public PlayExitAction(Play pPlay) {
		play = pPlay;
	}

	@Override
	public void execute(List<Character> characters) {
		List<String> names = new ArrayList<>();

		for (Character c: characters) {
			c.exit();
			names.add(c.getName());
		}

		if (names.size() == 1) {
			play.writeStageDirections("EXIT " + names.get(0));
			return;
		}
		
		final String LASTNAME = names.remove(names.size()-1);
		play.writeStageDirections("EXIT " + String.join(", ", names) + " AND " + LASTNAME);
	}
}
