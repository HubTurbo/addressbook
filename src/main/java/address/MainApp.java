package address;

import address.controller.MainController;
import address.events.EventManager;
import address.model.DataManager;
import address.storage.StorageManager;
import address.sync.SyncManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {


    protected StorageManager storageManager;
    protected DataManager dataManager;
    protected SyncManager syncManager;
    private MainController mainController;

    /**
     * Constructor
     */
    public MainApp() {
        setupComponents();
    }

    protected void setupComponents() {
        dataManager = new DataManager();
        storageManager = new StorageManager();
        syncManager = new SyncManager();
        mainController = new MainController(dataManager, storageManager);
        EventManager.getInstance().registerHandler(this);
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