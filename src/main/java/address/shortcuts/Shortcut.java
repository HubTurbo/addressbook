package address.shortcuts;

import address.events.BaseEvent;
import javafx.scene.input.KeyCombination;

/**
 * Represents a keyboard shortcut
 */
public class Shortcut {
    private KeyCombination keyCombination;
    private BaseEvent eventToRaise;

    Shortcut(KeyCombination keyCombination, BaseEvent eventToRaise) {
        this.keyCombination = keyCombination;
        this.eventToRaise = eventToRaise;
    }

    Shortcut(KeyCombination keyCombination) {
        this.keyCombination = keyCombination;
        this.eventToRaise = null;
    }

    public KeyCombination getKeyCombination() {
        return keyCombination;
    }

    public BaseEvent getEventToRaise() {
        return eventToRaise;
    }
}
