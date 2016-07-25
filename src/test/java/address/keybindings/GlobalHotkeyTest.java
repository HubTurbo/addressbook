package address.keybindings;

import address.testutil.TestUtil;
import javafx.scene.input.KeyCodeCombination;
import org.junit.Test;

import javax.swing.*;

import static org.junit.Assert.*;

public class GlobalHotkeyTest {
    GlobalHotkey globalHotkey = new GlobalHotkey("Sample hotkey",
                                                 KeyCodeCombination.valueOf("META + ALT + X"),
                                                 KeyBindingTest.SAMPLE_EVENT);
    @Test
    public void getKeyStroke() throws Exception {
        assertEquals(KeyStroke.getKeyStroke("meta alt X"), globalHotkey.getKeyStroke());
    }

    @Test
    public void toStringMethod() throws Exception {
        assertEquals(TestUtil.getOsDependentKeyCombinationString("Global Hotkey Sample hotkey Alt+Meta+X"), globalHotkey.toString());
    }

}