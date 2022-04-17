package playwriter;

import java.util.List;


/**
 * Represents a play action.
 */
public interface PlayAction {
	
	/**
	 * Executes the action.
	 */
	void execute(List<Character> characters);
}
