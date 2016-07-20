package address.keybindings;

import commons.OsDetector;
import org.junit.Test;

import static org.junit.Assert.*;

public class AcceleratorTest {
    private static final String SHIFT_STRING = OsDetector.isOnMac() ? "⇧" : "Shift+";

    Accelerator accelerator = new Accelerator("Dummy accelerator", KeyBindingTest.SHIFT_B);

    @Test
    public void toStringMethod() throws Exception {
        assertEquals("Accelerator Dummy accelerator " + SHIFT_STRING + "B", accelerator.toString());
    }

}