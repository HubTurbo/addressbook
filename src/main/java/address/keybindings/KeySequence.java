package address.keybindings;

import address.events.BaseEvent;
import javafx.scene.input.KeyCombination;

/**
 * Represents a shortcut that is a key sequence e.g. G followed by P
 */
public class KeySequence extends KeyBinding{

    /** Max delay (in milliseconds) allowed between key presses of a key sequence */
    public static final int KEY_SEQUENCE_MAX_DELAY_BETWEEN_KEYS = 1000;

    protected KeyCombination secondKeyCombination;

    public KeySequence(String name, KeyCombination firstKeyCombination,  BaseEvent eventToRaise) {
        super(name, firstKeyCombination, eventToRaise);
        assert false : "Invalid constructor called";
    }

    public KeySequence(String name, KeyCombination firstKeyCombination, KeyCombination secondKeyCombination,
                       BaseEvent eventToRaise) {
        super(name, firstKeyCombination, eventToRaise);
        this.secondKeyCombination = secondKeyCombination;
    }

    @Override
    public String toString(){
        return "Key sequence " + getDisplayText() + ", " + secondKeyCombination.getDisplayText();
    }

    /**
     * Returns true of the {@code otherKeyCombination} is exactly same as one of the key combinations
     *     in this key sequence.
     */
    public boolean isIncluded(KeyCombination otherKeyCombination) {
        return keyCombination.toString().equals(otherKeyCombination.toString())
                || secondKeyCombination.toString().equals(otherKeyCombination.toString());
        //TODO: make the comparison smarter so that it can detect a match between CTRL+X and SHORTCUT+X
    }
}
