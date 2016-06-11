package address.keybindings;

import address.events.BaseEvent;
import javafx.scene.input.KeyCombination;

import java.util.Optional;

/**
 * Parent class for different key binding types.
 */
public class KeyBinding {
    protected KeyCombination keyCombination;
    protected Optional<BaseEvent> eventToRaise;

    protected KeyBinding (KeyCombination keyCombination, BaseEvent eventToRaise) {
        this.keyCombination = keyCombination;
        this.eventToRaise = Optional.ofNullable(eventToRaise);
    }

    public KeyCombination getKeyCombination() {
        return keyCombination;
    }

    public Optional<BaseEvent> getEventToRaise(){
        return eventToRaise;
    }

}
