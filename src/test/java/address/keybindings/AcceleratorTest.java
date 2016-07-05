package address.keybindings;

import org.junit.Test;

import static org.junit.Assert.*;

public class AcceleratorTest {

    Accelerator accelerator = new Accelerator("Dummy accelerator", KeyBindingTest.SHIFT_B);

    @Test
    public void toStringMethod() throws Exception {
        assertEquals("Accelerator Dummy accelerator Shift+B", accelerator.toString());
    }

}