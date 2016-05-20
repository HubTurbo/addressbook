package address.guitests;

import address.TestApp;
import javafx.stage.Stage;
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
    public void setStage() throws TimeoutException {
        FxToolkit.setupStage(this::handleSetupStage);
    }

    @Before
    public void setup() throws Exception {
        FxToolkit.setupApplication(TestApp.class);
    }

    @After
    public void cleanup() throws TimeoutException {
        FxToolkit.cleanupStages();
    }

    private void handleSetupStage(Stage stage) {
    }
}
