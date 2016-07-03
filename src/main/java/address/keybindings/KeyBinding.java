package address.keybindings;

import address.events.BaseEvent;
import javafx.scene.input.KeyCombination;

/**
 * Parent class for different key binding types.
 */
public abstract class KeyBinding {
    protected String name;
    protected KeyCombination keyCombination;
    protected BaseEvent eventToRaise;

    protected KeyBinding (String name, KeyCombination keyCombination, BaseEvent eventToRaise) {
        assert name != null : "name cannot be null";
        assert keyCombination != null : "key combination cannot be null";
        assert eventToRaise != null : "event cannot be null";

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
