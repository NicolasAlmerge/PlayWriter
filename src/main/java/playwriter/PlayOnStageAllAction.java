package playwriter;

import static playwriter.Utils.check;

import java.util.List;


public final class PlayOnStageAllAction implements PlayAction {
	private final Play play;

	public PlayOnStageAllAction(Play pPlay) {
		play = pPlay;
	}

	@Override
	public void execute(List<Character> characters) {
		List<Character> remaining = play.getCharacters();
		
		for (CharacterView c: characters) {
			check(!c.hasEntered(), "cannot exclude character '" + c.getName() + "' since it has already entered");
			remaining.remove(c);
		}
		
		for (Character c: remaining) c.enter();
	}
}
