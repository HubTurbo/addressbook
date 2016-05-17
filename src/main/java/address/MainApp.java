package address;

import address.model.AddressBook;
import com.teamdev.jxbrowser.chromium.BrowserCore;
import com.teamdev.jxbrowser.chromium.internal.Environment;

import address.controller.MainController;
import address.events.EventManager;
import address.events.LoadDataRequestEvent;
import address.model.ModelManager;
import address.preferences.PreferencesManager;
import address.storage.StorageManager;
import address.sync.SyncManager;
import address.util.Config;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * The main entry point to the application.
 */
public class MainApp extends Application {

    protected Config config;
    protected StorageManager storageManager;
    protected ModelManager modelManager;
    protected SyncManager syncManager;
    private MainController mainController;

    public MainApp() {}

    protected Config getConfig() {
        return new Config();
    }

    @Override
    public void init() throws Exception {
        super.init();
        if (Environment.isMac()) {
            BrowserCore.initialize();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        setupComponents();
        mainController.start(primaryStage);

        EventManager.getInstance().post(new LoadDataRequestEvent(PreferencesManager.getInstance().getPersonFile()));
        syncManager.startSyncingData(config.updateInterval, config.simulateUnreliableNetwork);
    }

    protected void setupComponents() {
        config = getConfig();
        PreferencesManager.setAppTitle(config.appTitle);

        modelManager = new ModelManager(new AddressBook());
        storageManager = new StorageManager(modelManager);
        mainController = new MainController(this, modelManager, config);
        syncManager = new SyncManager();
    }

    @Override
    public void stop() {
        mainController.getPrimaryStage().hide();
        mainController.freeBrowserResources();
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
