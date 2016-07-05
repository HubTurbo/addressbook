package address.keybindings;

import address.events.KeyBindingEvent;
import javafx.scene.input.KeyCodeCombination;
import org.junit.Test;

import static org.junit.Assert.*;

public class BindingsTest {

    private Bindings bindings = new Bindings();

    @Test
    public void findMatchingSequence() throws Exception {
        KeyBindingEvent keyEventG = getKeyEvent("G");
        KeyBindingEvent keyEventB = getKeyEvent("B");
        assertTrue(KeySequence.isElapsedTimePermissibile(keyEventG.time, keyEventB.time));
        assertEquals(bindings.LIST_GOTO_BOTTOM_SEQUENCE, bindings.findMatchingSequence(keyEventG, keyEventB).get());
    }

    private KeyBindingEvent getKeyEvent(String keyCombo) {
        return new KeyBindingEvent(KeyCodeCombination.valueOf(keyCombo));
    }

    @Test
    public void getHotkeys() throws Exception {
        assertEquals(bindings.hotkeys, bindings.getHotkeys());
    }

    @Test
    public void getAccelerators() throws Exception {
        assertEquals(bindings.accelerators, bindings.getAccelerators());
    }

    @Test
    public void getSequences() throws Exception {
        assertEquals(bindings.sequences, bindings.getSequences());
    }

    @Test
    public void getBinding() throws Exception {
        //verifying a sequence
        KeyBindingEvent keyEventG = getKeyEvent("G");
        KeyBindingEvent keyEventB = getKeyEvent("B");
        assertTrue(KeySequence.isElapsedTimePermissibile(keyEventG.time, keyEventB.time));
        assertEquals(bindings.LIST_GOTO_BOTTOM_SEQUENCE, bindings.getBinding(keyEventG, keyEventB).get());

        //verifyng a shortcut
        KeyBindingEvent keyEventShortcutDown = getKeyEvent("SHORTCUT + DOWN");
        assertEquals(bindings.LIST_ENTER_SHORTCUT, bindings.getBinding(keyEventB, keyEventShortcutDown).get());

        //verifying an accelerator
        KeyBindingEvent keyEventD = getKeyEvent("D");
        assertEquals(bindings.PERSON_DELETE_ACCELERATOR, bindings.getBinding(keyEventShortcutDown, keyEventD).get());

        //verifying an hotkey
        KeyBindingEvent keyEventMetaAltX = getKeyEvent("META + ALT + X");
        assertEquals(bindings.APP_MINIMIZE_HOTKEY.get(1), bindings.getBinding(keyEventD, keyEventMetaAltX).get());
    }

    @Test
    public void getAllBindings() throws Exception {
        int totalBindings = bindings.accelerators.size()
                            + bindings.hotkeys.size()
                            + bindings.sequences.size()
                            + bindings.shortcuts.size();
        assertEquals(totalBindings, bindings.getAllBindings().size());
    }

}