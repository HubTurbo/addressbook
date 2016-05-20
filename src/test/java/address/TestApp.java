package address;


import address.util.Config;
import com.google.common.util.concurrent.SettableFuture;
import javafx.stage.Stage;

public class TestApp extends MainApp {

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
