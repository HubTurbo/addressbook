package address.keybindings;

import address.events.BaseEvent;
import javafx.scene.input.KeyCombination;

import java.util.Optional;

/**
 * Parent class for different key binding types.
 */
public class KeyBinding {
    protected String name;
    protected KeyCombination keyCombination;
    protected BaseEvent eventToRaise;

    protected KeyBinding (String name, KeyCombination keyCombination, BaseEvent eventToRaise) {
        this.name = name;
        this.keyCombination = keyCombination;
        this.eventToRaise = eventToRaise;
    }

    public KeyCombination getKeyCombination() {
        return keyCombination;
    }

    public BaseEvent getEventToRaise(){
        return eventToRaise;
    }

    public String getName() { return name; }

    protected String getDisplayText(){
        return getName() + " " + keyCombination.getDisplayText();
    }

}
