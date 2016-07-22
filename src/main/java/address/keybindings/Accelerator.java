package address.keybindings;

import address.events.hotkey.AcceleratorIgnoredEvent;
import javafx.scene.input.KeyCombination;

/**
 * Represents a shortcut that is also a keyboard accelerator.
 */
public class Accelerator extends KeyBinding{

    public Accelerator(String name, KeyCombination keyCombination) {
        super(name, keyCombination, new AcceleratorIgnoredEvent(name));
    }

    @Override
    public String toString(){
        return "Accelerator " + getDisplayText();
    }

}
