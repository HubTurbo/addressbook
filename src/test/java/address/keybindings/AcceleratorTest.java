package address.keybindings;

import address.testutil.TestUtil;
import commons.OsDetector;
import org.junit.Test;

import static org.junit.Assert.*;

public class AcceleratorTest {
    Accelerator accelerator = new Accelerator("Dummy accelerator", KeyBindingTest.SHIFT_B);

    @Test
    public void toStringMethod() throws Exception {
        assertEquals(TestUtil.getOsDependentKeyCombinationString("Accelerator Dummy accelerator Shift+B"), accelerator.toString());
    }

}