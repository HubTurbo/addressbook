package address;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserCore;
import com.teamdev.jxbrowser.chromium.internal.Environment;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;

import address.controller.MainController;
import address.model.AddressBookWrapper;
import address.model.ModelManager;
import address.preferences.PreferencesManager;
import address.storage.StorageManager;
import address.sync.SyncManager;
import address.util.Config;
import javafx.application.Application;
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

    }

    protected void setupComponents() {
        config = getConfig();
        PreferencesManager.setAppTitle(config.appTitle);
        AddressBookWrapper dataFromFile = StorageManager.getDataFromFile(
                                                PreferencesManager.getInstance().getPersonFile());
        modelManager = new ModelManager(dataFromFile);
        storageManager = new StorageManager(modelManager);
        mainController = new MainController(this, modelManager, config);
        syncManager = new SyncManager();
        syncManager.startSyncingData(config.updateInterval, config.isSimulateRandomChanges);
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
