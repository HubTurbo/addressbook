package address.keybindings;

import address.events.KeyBindingEvent;

import java.util.List;
import java.util.Optional;

/**
 * An extension to {@link KeyBindingsManager} to provide additional
 * convenience methods for testing.
 */
public class KeyBindingsManagerEx extends KeyBindingsManager {

    /**
     * Returns the key binding that matches the given two keyevents, if any.
     * @param currentEvent the most recent key event
     * @param previousEvent the previous key event
     */
    public Optional<? extends KeyBinding> getBinding(KeyBindingEvent previousEvent, KeyBindingEvent currentEvent) {
        return BINDINGS.getBinding(previousEvent, currentEvent);
    }

    /** Returns {@link address.keybindings.KeyBinding} objects managed.
     */
    public List<KeyBinding> getAllKeyBindings() {
        return BINDINGS.getAllBindings();
    }
}
