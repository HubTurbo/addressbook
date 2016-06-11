package address.keybindings;

import javafx.scene.input.KeyCombination;

import java.util.Optional;

/**
 * Represents a shortcut that is also a keyboard accelerator.
 */
public class Accelerator extends KeyBinding{


    Accelerator(String name, KeyCombination keyCombination) {
        super(name, keyCombination, null);
    }

    @Override
    public String toString(){
        return "Accelerator " + getDisplayText();
    }

}
