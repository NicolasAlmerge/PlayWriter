package playwriter;

import java.util.List;


public final class PlayOnStageAction implements PlayAction {
	private final Play play;

	public PlayOnStageAction(Play pPlay) {
		play = pPlay;
	}

	@Override
	public void execute(List<Character> characters) {
		for (Character c: characters) c.enter();
		play.resetWidth();
	}
}
