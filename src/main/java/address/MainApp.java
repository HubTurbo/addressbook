package address;

import address.controller.MainController;
import address.events.EventManager;
import address.events.FileOpeningExceptionEvent;
import address.exceptions.FileContainsDuplicatesException;
import address.model.AddressBookWrapper;
import address.model.ModelManager;
import address.preferences.PreferencesManager;
import address.storage.StorageManager;
import address.sync.SyncManager;
import address.util.Config;
import javafx.application.Application;
import javafx.stage.Stage;

import javax.xml.bind.JAXBException;
import java.io.File;

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
    public void start(Stage primaryStage) {
        setupComponents();
        mainController.start(primaryStage);
    }

    protected void setupComponents() {

        config = getConfig();
        PreferencesManager.setAppTitle(config.appTitle);

        final File initialFile = PreferencesManager.getInstance().getPersonFile();
        AddressBookWrapper dataFromFile;
        Exception fileLoadException = null;
        try {
             dataFromFile = StorageManager.loadDataFromSaveFile(initialFile);
        } catch (JAXBException | FileContainsDuplicatesException e) {
            fileLoadException = e;
            dataFromFile = new AddressBookWrapper();
        }

        modelManager = new ModelManager(dataFromFile);
        storageManager = new StorageManager(modelManager);
        mainController = new MainController(this, modelManager, config);
        syncManager = new SyncManager();
        syncManager.startSyncingData(config.updateInterval, config.isSimulateRandomChanges);

        if (fileLoadException != null) {
            EventManager.getInstance().post(new FileOpeningExceptionEvent(fileLoadException, initialFile));
        }
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
