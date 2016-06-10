package address.keybindings;

import javafx.scene.input.KeyCombination;

/**
 * Represents a shortcut that is also a keyboard accelerator.
 */
public class Accelerator extends KeyBinding{


    Accelerator(KeyCombination keyCombination) {
        super(keyCombination);
    }

}
