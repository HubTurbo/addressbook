package address.guitests;

import address.TestApp;
import address.events.EventManager;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;

import java.util.concurrent.TimeoutException;

public class GuiTestBase extends FxRobot {
    @BeforeClass
    public static void setupSpec() {
        try {
            FxToolkit.registerPrimaryStage();
            FxToolkit.hideStage();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setup() throws Exception {
        EventManager.clearSubscribers();
        FxToolkit.setupApplication(TestApp.class);
    }

    @After
    public void cleanup() throws TimeoutException {
        FxToolkit.cleanupStages();
    }
}
