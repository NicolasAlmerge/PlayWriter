package playwriter;

import static playwriter.Utils.check;


/**
 * Represents a character.
 */
public final class Character implements CharacterView {
    private final String name;
    private final String description;
    private boolean entered = false;

    /**
     * Constructor.
     */
    public Character(String pName, String pDescription) {
        name = pName;
        description = (pDescription == null)? "": pDescription;
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

    public void enter() {
    	check(!entered, "cannot make character " + name + " enter as it is already on stage");
        entered = true;
    }

    public void exit() {
    	check(entered, "cannot make character " + name + " exit as it is not on stage");
        entered = false;
    }
}
