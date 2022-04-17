package playwriter;


/**
 * Represents a read-only view of a character.
 */
public interface CharacterView {

	/**
	 * @return Character's name.
	 */
	String getName();

	/**
	 * @return Character's description.
	 */
	String getDescription();

	/**
	 * @return True if character has entered, false otherwise.
	 */
	boolean hasEntered();
}
