package address.shortcuts;

import javafx.scene.input.KeyCombination;

/**
 * Represents a keyboard shortcut
 */
public class Shortcut {
    KeyCombination keyCombination;
    Runnable action;

    Shortcut(KeyCombination keyCombination, Runnable action) {
        this.keyCombination = keyCombination;
        this.action = action;
    }
}
