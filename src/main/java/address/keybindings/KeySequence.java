package address.keybindings;

import address.events.BaseEvent;
import javafx.scene.input.KeyCombination;

/**
 * Represents a shortcut that is a key sequence e.g. G followed by P
 */
public class KeySequence extends KeyBinding{

    protected KeyCombination secondKeyCombination;
    protected BaseEvent eventToRaise;

    public KeySequence(KeyCombination firstKeyCombination, KeyCombination secondKeyCombination, BaseEvent eventToRaise) {
        super(firstKeyCombination);
        this.secondKeyCombination = secondKeyCombination;
        this.eventToRaise = eventToRaise;
    }
}
