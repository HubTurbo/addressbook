package address;

import address.controller.MainController;
import address.model.ModelManager;
import address.storage.StorageManager;
import address.sync.SyncManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {


    protected StorageManager storageManager;
    protected ModelManager modelManager;
    protected SyncManager syncManager;
    private MainController mainController;

    /**
     * Constructor
     */
    public MainApp() {
        setupComponents();
    }

    protected void setupComponents() {
        modelManager = new ModelManager();
        storageManager = new StorageManager();
        syncManager = new SyncManager();
        mainController = new MainController(modelManager, storageManager);
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