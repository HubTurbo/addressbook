package address.keybindings;

import javafx.scene.input.KeyCombination;

/**
 * Represents a shortcut that is also a keyboard accelerator.
 */
public class Accelerator {
    private KeyCombination keyCombination;

    Accelerator(KeyCombination keyCombination) {
        this.keyCombination = keyCombination;
    }

    public KeyCombination getKeyCombination() {
        return keyCombination;
    }
}
