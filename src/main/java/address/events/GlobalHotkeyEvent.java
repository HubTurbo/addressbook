package address.events;

import javafx.scene.input.KeyCombination;

/**
 * Represents a global hotkey event
 */
public class GlobalHotkeyEvent extends BaseEvent {

    public final PotentialKeyboardShortcutEvent keyboardShortcutEvent;

    public GlobalHotkeyEvent(KeyCombination keyCombination){
        this.keyboardShortcutEvent = new PotentialKeyboardShortcutEvent(keyCombination);
    }

    @Override
    public String toString(){
        final String className = this.getClass().getSimpleName();
        return className + " : keyCombination is " + keyboardShortcutEvent.keyCombination.get().getDisplayText();
    }
}
