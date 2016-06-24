package address;

import address.model.UserPrefs;
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
    protected Config initConfig() {
        Config config = super.initConfig();
        config.appTitle = "Test App";
        return config;
    }

    @Override
    protected UserPrefs initPrefs(Config config) {
        UserPrefs userPrefs = super.initPrefs(config);
        userPrefs.setSaveLocation(saveLocationForTesting);
        return userPrefs;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
