package address.keybindings;

import address.events.BaseEvent;
import javafx.scene.input.KeyCombination;

/**
 * Represents a keyboard shortcut
 */
public class Shortcut extends KeyBinding{
    private BaseEvent eventToRaise;

    Shortcut(KeyCombination keyCombination, BaseEvent eventToRaise) {
        super(keyCombination);
        this.eventToRaise = eventToRaise;
    }

    public KeyCombination getKeyCombination() {
        return keyCombination;
    }

    public BaseEvent getEventToRaise() {
        return eventToRaise;
    }
}
