package address.keybindings;

import org.junit.Test;

import static org.junit.Assert.*;


public class ShortcutTest {

    Shortcut shortcut = new Shortcut("Dummy shortcut", KeyBindingTest.ALT_A, KeyBindingTest.SAMPLE_EVENT);

    @Test
    public void getKeyCombination() throws Exception {
        assertEquals("Alt+A", shortcut.getKeyCombination().getDisplayText());
    }

    @Test
    public void toStringMethod() throws Exception {
        assertEquals("Keyboard shortcut Dummy shortcut Alt+A", shortcut.toString());
    }
}