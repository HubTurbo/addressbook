package address.keybindings;


import address.events.AcceleratorIgnoredEvent;
import address.events.BaseEvent;
import address.testutil.TestUtil;
import commons.OsDetector;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class KeyBindingTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    static KeyCombination ALT_A = KeyCodeCombination.valueOf("ALT + A");
    static KeyCombination SHIFT_B = KeyCodeCombination.valueOf("SHIFT + B");
    static BaseEvent SAMPLE_EVENT = new AcceleratorIgnoredEvent("Dummy");

    private final String SAMPLE_KEYBINDING_NAME = "dummy name";
    private KeyBinding keyBinding = new Shortcut(SAMPLE_KEYBINDING_NAME, ALT_A, SAMPLE_EVENT);

    @Test
    public void constructor_nullParameters_assertionFailure() {
        // Null name
        thrown.expect(AssertionError.class);
        thrown.expectMessage("name cannot be null");
        new Shortcut(null, ALT_A, SAMPLE_EVENT);

        // Null key combo
        thrown.expect(AssertionError.class);
        thrown.expectMessage("key combination cannot be null");
        new Shortcut(SAMPLE_KEYBINDING_NAME, null, SAMPLE_EVENT);

        // Null event
        thrown.expect(AssertionError.class);
        thrown.expectMessage("event cannot be null");
        new Shortcut(SAMPLE_KEYBINDING_NAME, SHIFT_B, null);
    }

    @Test
    public void getText() {
        assertEquals(TestUtil.getOsDependentKeyCombinationString(SAMPLE_KEYBINDING_NAME + " Alt+A"), keyBinding.getDisplayText());
    }

    @Test
    public void getKeyCombination() throws Exception {
        assertEquals(ALT_A, keyBinding.getKeyCombination());
    }

    @Test
    public void getName() throws Exception {
        assertEquals(SAMPLE_KEYBINDING_NAME, keyBinding.getName());
    }

    @Test
    public void getEventToRaise() throws Exception {
        assertEquals(SAMPLE_EVENT, keyBinding.getEventToRaise());
    }

}
