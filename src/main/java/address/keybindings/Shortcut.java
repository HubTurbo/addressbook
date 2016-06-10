package address.keybindings;

import address.events.BaseEvent;
import javafx.scene.input.KeyCombination;

/**
 * Represents a keyboard shortcut
 */
public class Shortcut extends KeyBinding{

    Shortcut(KeyCombination keyCombination, BaseEvent eventToRaise) {
        super(keyCombination, eventToRaise);
    }

    public KeyCombination getKeyCombination() {
        return keyCombination;
    }

}
