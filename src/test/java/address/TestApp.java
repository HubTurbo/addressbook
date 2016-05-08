package address;


import address.MainApp;
import address.util.Config;

public class TestApp extends MainApp {
    public TestApp(){
        super();
    }

    @Override
    protected void setupComponents(){
        super.setupComponents();
    }

    @Override
    protected Config getConfig() {
        Config testConfig = new Config();
        testConfig.appTitle = "Test App";
        testConfig.updateInterval = 0;
        return testConfig;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
