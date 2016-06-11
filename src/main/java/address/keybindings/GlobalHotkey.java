package address.keybindings;

import address.events.BaseEvent;
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
        map.put(KeyCodeCombination.valueOf("Ctrl + Alt + X"), KeyStroke.getKeyStroke("control alt X") );
        map.put(KeyCodeCombination.valueOf("Meta + Alt + X"), KeyStroke.getKeyStroke("meta alt X") );
        map.put(KeyCodeCombination.valueOf("Ctrl + Shift + X"), KeyStroke.getKeyStroke("control shift X") );
        map.put(KeyCodeCombination.valueOf("Meta + Shift + X"), KeyStroke.getKeyStroke("meta shift X") );
    }

    private KeyStroke keyStroke;


    public GlobalHotkey(KeyCombination keyCombination, BaseEvent eventToRaise){
        super(keyCombination, eventToRaise);
        this.keyStroke = getKeyStroke(keyCombination);
    }

    protected KeyStroke getKeyStroke() {
        return keyStroke;
    }

    private static KeyStroke getKeyStroke(KeyCombination keyCombination){
        KeyStroke keyStroke = map.get(keyCombination);
        assert keyStroke != null;
        return keyStroke;
    }

    @Override
    public String toString(){
        return "Global Hotkey " + keyCombination.getDisplayText();
    }

}
