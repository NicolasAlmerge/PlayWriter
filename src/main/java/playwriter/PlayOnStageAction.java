package playwriter;

import java.util.List;


public final class PlayOnStageAction implements PlayAction {
	@Override
	public void execute(List<Character> characters) {
		for (Character c: characters) c.enter();
	}
}
