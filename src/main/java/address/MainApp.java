package address;

import address.controller.MainController;
import address.model.ModelManager;
import address.preferences.PreferencesManager;
import address.storage.StorageManager;
import address.sync.SyncManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The main entry point to the application.
 */
public class MainApp extends Application {


    protected StorageManager storageManager;
    protected ModelManager modelManager;
    protected SyncManager syncManager;
    protected PreferencesManager preferencesManager;
    private MainController mainController;

    public MainApp() {
        setupComponents();
    }

    protected void setupComponents() {
        preferencesManager = PreferencesManager.getInstance();
        storageManager = new StorageManager();
        modelManager = new ModelManager(storageManager.getPersonDataFromFile(preferencesManager.getPersonFilePath()));
        storageManager.setModel(modelManager);
        mainController = new MainController(modelManager);
        syncManager = new SyncManager();
        syncManager.startSyncingData(5);
    }


    @Override
    public void start(Stage primaryStage) {
        mainController.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}