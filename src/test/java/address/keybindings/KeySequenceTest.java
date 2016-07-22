package address.keybindings;


import address.testutil.TestUtil;
import javafx.scene.input.KeyCodeCombination;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KeySequenceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    @SuppressWarnings(value = "Used to test deprecated method")
    public void constructor_invalidConstructor_assertionFailure() throws Exception {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("Invalid constructor called");
        new KeySequence("dummy name", KeyBindingTest.ALT_A, KeyBindingTest.SAMPLE_EVENT);
    }

    @Test
    public void constructor_nullSecondKeyCombo_assertionFailure() throws Exception {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("Second key combination cannot be null");
        new KeySequence("dummy name", KeyBindingTest.ALT_A, null, KeyBindingTest.SAMPLE_EVENT);
    }

    @Test
    public void getSecondKeyCombination() throws Exception {
        KeySequence keySequence = new KeySequence("Sample Key Sequence", KeyBindingTest.ALT_A, KeyBindingTest.SHIFT_B, KeyBindingTest.SAMPLE_EVENT);
        assertEquals(TestUtil.getOsDependentKeyCombinationString("Shift+B"), keySequence.getSecondKeyCombination().getDisplayText());
    }

    @Test
    public void toStringMethod() throws Exception {
        KeySequence keySequence = new KeySequence("Sample Key Sequence", KeyBindingTest.ALT_A, KeyBindingTest.SHIFT_B, KeyBindingTest.SAMPLE_EVENT);
        assertEquals(TestUtil.getOsDependentKeyCombinationString("Key sequence Sample Key Sequence Alt+A, Shift+B"), keySequence.toString());
    }

    @Test
    public void isIncluded() throws Exception {
        KeySequence keySequence = new KeySequence("Sample Key Sequence", KeyBindingTest.ALT_A, KeyBindingTest.SHIFT_B, KeyBindingTest.SAMPLE_EVENT);

        assertFalse(keySequence.isIncluded(null));

        assertTrue(keySequence.isIncluded(KeyBindingTest.ALT_A));
        assertTrue(keySequence.isIncluded(KeyBindingTest.SHIFT_B));

        assertFalse(keySequence.isIncluded(KeyCodeCombination.valueOf("SHIFT + A")));
    }

    @Test
    public void isElapsedTimePermissibile_validCases() throws Exception {

        long baseTime = 32322;
        long permissibleDelayInNanoSeconds =
                NANOSECONDS.convert(KeySequence.KEY_SEQUENCE_MAX_MILLISECONDS_BETWEEN_KEYS, MILLISECONDS);

        // Elapsed time is 0
        assertTrue(KeySequence.isElapsedTimePermissibile(baseTime, baseTime));

        // Elapsed time is exactly KeySequence.KEY_SEQUENCE_MAX_MILLISECONDS_BETWEEN_KEYS
        assertTrue(KeySequence.isElapsedTimePermissibile(baseTime,
                baseTime + permissibleDelayInNanoSeconds));

        // Boundary case: Elapsed time is permitted time + 1
        assertFalse(KeySequence.isElapsedTimePermissibile(baseTime,
                baseTime + permissibleDelayInNanoSeconds + 1));

    }
    @Test
    public void isElapsedTimePermissibile_negativeDuration_assertionError() throws Exception {
        long baseTime = 32322;
        thrown.expect(AssertionError.class);
        thrown.expectMessage("second key event cannot happen before the first one");
        KeySequence.isElapsedTimePermissibile(baseTime, baseTime-1);
    }

    @Test
    public void isElapsedTimePermissibile_negativeFirstParam_assertionError() throws Exception {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("times cannot be negative");
        KeySequence.isElapsedTimePermissibile(-1, 0);
    }

    @Test
    public void isElapsedTimePermissibile_negativeSecondParam_assertionError() throws Exception {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("times cannot be negative");
        KeySequence.isElapsedTimePermissibile(0, -1);
    }
}
