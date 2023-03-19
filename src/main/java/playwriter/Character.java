package playwriter;

/**
 * Represents a character.
 */
public final class Character implements CharacterView {
  private final String name;
  private final String description;
  private boolean entered = false;

  /**
   * Constructor.
   *
   * @param cname        Character name.
   * @param cdescription Character description.
   */
  public Character(String cname, String cdescription) {
    name = cname;
    description = (cdescription == null) ? "" : cdescription;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public boolean hasEntered() {
    return entered;
  }

  /**
   * Makes the character enter.
   *
   * @throws PlayCompileTimeError if character was already on stage.
   */
  public void enter() throws PlayCompileTimeError {
    Utils.check(!entered, "cannot make character " + name + " enter as it is already on stage");
    entered = true;
  }

  /**
   * Makes the character exit.
   *
   * @throws PlayCompileTimeError if character was already not on stage.
   */
  public void exit() throws PlayCompileTimeError {
    Utils.check(entered, "cannot make character " + name + " exit as it is not on stage");
    entered = false;
  }

  /**
   * Makes the character exit. If the character was not already on stage, this has no effects.
   */
  public void forceExit() {
    entered = false;
  }
}
