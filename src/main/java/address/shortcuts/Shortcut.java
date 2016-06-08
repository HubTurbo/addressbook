package address.shortcuts;

import javafx.scene.input.KeyCombination;

/**
 * Represents a keyboard shortcut
 */
public class Shortcut {
    private KeyCombination keyCombination;
    private Runnable action;

    Shortcut(KeyCombination keyCombination, Runnable action) {
        this.keyCombination = keyCombination;
        this.action = action;
    }

    public KeyCombination getKeyCombination() {
        return keyCombination;
    }

    public Runnable getAction() {
        return action;
    }
}
