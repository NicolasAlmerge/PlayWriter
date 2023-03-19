package playwriter;

/**
 * Represents a read-only view of a character.
 *
 * @author Nicolas Almerge
 * @since 1.0
 */
public interface CharacterView {

  /**
   * Gets a character's name.
   *
   * @return Character's name.
   */
  String getName();

  /**
   * Gets a character's description.
   *
   * @return Character's description.
   */
  String getDescription();

  /**
   * Checks whether the character has entered or not.
   *
   * @return <code>true</code> if character has entered, <code>false</code> otherwise.
   */
  boolean hasEntered();
}
