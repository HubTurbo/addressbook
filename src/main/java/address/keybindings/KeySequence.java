package address.keybindings;

import address.events.BaseEvent;
import javafx.scene.input.KeyCode;

/**
 * Represents a shortcut that is a key sequence e.g. G followed by P
 */
public class KeySequence {
    protected KeyCode firstKey;
    protected KeyCode secondKey;
    protected BaseEvent eventToRaise;

    public KeySequence(KeyCode firstKey, KeyCode secondKey, BaseEvent eventToRaise) {
        this.firstKey = firstKey;
        this.secondKey = secondKey;
        this.eventToRaise = eventToRaise;
    }
}
