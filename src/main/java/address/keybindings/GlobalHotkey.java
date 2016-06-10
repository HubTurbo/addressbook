package address.keybindings;

import address.events.BaseEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a global hotkey.
 */
public class GlobalHotkey extends KeyBinding{
    private static Map<KeyCombination, KeyStroke> map = new HashMap<>();
    static{
        map.put(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN),
                KeyStroke.getKeyStroke("control alt X") );
        map.put(new KeyCodeCombination(KeyCode.X, KeyCombination.META_DOWN, KeyCombination.ALT_DOWN),
                KeyStroke.getKeyStroke("meta alt X") );
        map.put(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN),
                KeyStroke.getKeyStroke("control shift X") );
        map.put(new KeyCodeCombination(KeyCode.X, KeyCombination.META_DOWN, KeyCombination.SHIFT_DOWN),
                KeyStroke.getKeyStroke("meta shift X") );
    }

    private KeyStroke keyStroke;


    public GlobalHotkey(KeyCombination keyCombination, BaseEvent eventToRaise){
        super(keyCombination, eventToRaise);
        this.keyStroke = getKeyStroke(keyCombination);
    }

    protected KeyStroke getKeyStroke() {
        return keyStroke;
    }

    public static KeyStroke getKeyStroke(KeyCombination keyCombination){
        KeyStroke keyStroke = map.get(keyCombination);
        assert keyStroke != null;
        return keyStroke;
    }
}
