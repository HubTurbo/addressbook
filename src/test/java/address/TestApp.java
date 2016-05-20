package address;


import address.util.Config;
import com.google.common.util.concurrent.SettableFuture;
import javafx.stage.Stage;

public class TestApp extends MainApp {

    protected static final SettableFuture<Stage> STAGE_FUTURE = SettableFuture.create();
    @Override
    protected Config getConfig() {
        Config testConfig = new Config();
        testConfig.appTitle = "Test App";
        testConfig.updateInterval = 0;
        return testConfig;
    }

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);
        STAGE_FUTURE.set(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
