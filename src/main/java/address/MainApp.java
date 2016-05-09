package address;

import address.controller.MainController;
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
    protected PreferencesManager preferencesManager;
    private MainController mainController;

    public MainApp() {
        setupComponents();
    }

    protected void setupComponents() {
        config = getConfig();
        preferencesManager = PreferencesManager.getInstance();
        storageManager = new StorageManager();
        modelManager = new ModelManager(storageManager.getPersonDataFromFile(preferencesManager.getPersonFilePath()));
        storageManager.setModel(modelManager);
        mainController = new MainController(modelManager, config);
        syncManager = new SyncManager();
        syncManager.startSyncingData(config.updateInterval, config.isSimulateRandomChanges);
    }

    protected Config getConfig() {
        return new Config();
    }


    @Override
    public void start(Stage primaryStage) {
        mainController.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}