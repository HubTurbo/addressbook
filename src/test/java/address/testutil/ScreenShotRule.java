package address.testutil;

import com.google.common.io.Files;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.loadui.testfx.GuiTest;

import java.io.File;
import java.io.IOException;

/**
 * Capture screenshot after each failed test
 */
public class ScreenShotRule extends TestWatcher{

    @Override
    protected void failed(Throwable e, Description description) {
        File file = GuiTest.captureScreenshot();
        try {
            Files.copy(file, new File(description.getClassName() + description.getMethodName() + file.getName()));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
