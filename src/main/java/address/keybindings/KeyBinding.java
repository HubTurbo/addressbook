package address.keybindings;

import javafx.scene.input.KeyCombination;

/**
 * Parent class for different key binding types.
 */
public class KeyBinding {
    protected KeyCombination keyCombination;

    protected KeyBinding (KeyCombination keyCombination) {
        this.keyCombination = keyCombination;
    }

    public KeyCombination getKeyCombination() {
        return keyCombination;
    }
}
