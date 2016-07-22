package address.keybindings;

import address.testutil.TestUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ShortcutTest {

    Shortcut shortcut = new Shortcut("Dummy shortcut", KeyBindingTest.ALT_A, KeyBindingTest.SAMPLE_EVENT);

    @Test
    public void getKeyCombination() throws Exception {
        assertEquals(TestUtil.getOsDependentKeyCombinationString("Alt+A"), shortcut.getKeyCombination().getDisplayText());
    }

    @Test
    public void toStringMethod() throws Exception {
        assertEquals(TestUtil.getOsDependentKeyCombinationString("Keyboard shortcut Dummy shortcut Alt+A"), shortcut.toString());
    }
}