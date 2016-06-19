package address.unittests.keybindings;

import address.events.KeyBindingEvent;
import address.keybindings.KeyBinding;
import address.keybindings.KeyBindingsManager;

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
    public Optional<? extends KeyBinding> getBinding(KeyBindingEvent currentEvent, KeyBindingEvent previousEvent) {
        return BINDINGS.getBinding(currentEvent, previousEvent);
    }

    /** Returns {@link address.keybindings.KeyBinding} objects managed.
     */
    public List<KeyBinding> getAllKeyBindings() {
        return BINDINGS.getAllBindings();
    }
}
