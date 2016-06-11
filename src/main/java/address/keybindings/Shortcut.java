package address.keybindings;

import address.events.BaseEvent;
import javafx.scene.input.KeyCombination;

/**
 * Represents a normal keyboard shortcut
 */
public class Shortcut extends KeyBinding{

    Shortcut(String name, KeyCombination keyCombination, BaseEvent eventToRaise) {
        super(name, keyCombination, eventToRaise);
    }

    public KeyCombination getKeyCombination() {
        return keyCombination;
    }


    @Override
    public String toString(){
        return "Keyboard shortcut " + getDisplayText();
    }

}
