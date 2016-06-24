package address;

import address.model.UserPrefs;
import address.util.Config;
import address.util.TestUtil;

public class TestApp extends MainApp {

    public static final String SAVE_LOCATION_FOR_TESTING = TestUtil.appendToSandboxPath("sampleData.xml");

    public TestApp(){
        super();
        stageTestScenario();
    }

    protected void stageTestScenario() {
        TestUtil.createDataFileWithSampleData(SAVE_LOCATION_FOR_TESTING);
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
        userPrefs.setSaveLocation(SAVE_LOCATION_FOR_TESTING);
        return userPrefs;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
