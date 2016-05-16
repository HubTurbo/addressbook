package address.events;

import javafx.scene.input.KeyEvent;

/**
 * Indicates a key event occurred that is potentially a keyboard shortcut
 */
public class PotentialKeyboardShortcutEvent {

    /** The key event */
    public KeyEvent keyEvent;

    public PotentialKeyboardShortcutEvent(KeyEvent keyEvent){
        this.keyEvent = keyEvent;
    }

    @Override
    public String toString(){
        final String className = this.getClass().getSimpleName();
        return className + " : keyEvent is " + keyEvent.getCode();
    }
}
