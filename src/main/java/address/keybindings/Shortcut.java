package address.keybindings;

import address.events.BaseEvent;
import javafx.scene.input.KeyCombination;

/**
 * Represents a normal keyboard shortcut
 */
public class Shortcut extends KeyBinding{

    public Shortcut(String name, KeyCombination keyCombination, BaseEvent eventToRaise) {
        super(name, keyCombination, eventToRaise);
    }

    @Override
    public String toString(){
        return "Keyboard shortcut " + getDisplayText();
    }

}
