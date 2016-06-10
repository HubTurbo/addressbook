package address.keybindings;

import javafx.scene.input.KeyCombination;

import java.util.Optional;

/**
 * Represents a shortcut that is also a keyboard accelerator.
 */
public class Accelerator extends KeyBinding{


    Accelerator(KeyCombination keyCombination) {
        super(keyCombination, null);
    }

    @Override
    public Optional<String> getWhyNoEvent(){
        return Optional.of("Key binding " + keyCombination.getDisplayText()
                + "is an accelerator handled elsewhere in the app");
    }

}
