package address;

import address.util.Config;
import address.util.TestUtil;

import java.io.File;

public class TestApp extends MainApp {

    String saveLocationForTesting = TestUtil.appendToSandboxPath("sampleData.xml");

    public TestApp(){
        super();
        stageTestScenario();
    }

    protected void stageTestScenario() {
        TestUtil.createDataFileWithSampleData(saveLocationForTesting);
    }

    @Override
    protected void initConfig() {
        super.initConfig();
        config.appTitle = "Test App";
    }

    @Override
    protected void initPrefs() {
        modelManager.setPrefsSaveLocation(saveLocationForTesting);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
