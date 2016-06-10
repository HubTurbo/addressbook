package address.keybindings;

import address.events.BaseEvent;
import javafx.scene.input.KeyCombination;

/**
 * Represents a shortcut that is a key sequence e.g. G followed by P
 */
public class KeySequence extends KeyBinding{

    protected KeyCombination secondKeyCombination;

    public KeySequence(KeyCombination firstKeyCombination,  BaseEvent eventToRaise) {
        super(firstKeyCombination, eventToRaise);
        assert false : "Invalid constructor called";
    }

    public KeySequence(KeyCombination firstKeyCombination, KeyCombination secondKeyCombination, BaseEvent eventToRaise) {
        super(firstKeyCombination, eventToRaise);
        this.secondKeyCombination = secondKeyCombination;
    }
}
