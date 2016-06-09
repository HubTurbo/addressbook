package address;

import address.browser.BrowserManager;

import address.controller.MainController;
import address.events.EventManager;
import address.events.LoadDataRequestEvent;
import address.model.ModelManager;
import address.shortcuts.ShortcutsManager;
import address.prefs.PrefsManager;
import address.storage.StorageManager;
import address.sync.SyncManager;
import address.updater.UpdateManager;
import address.util.Config;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * The main entry point to the application.
 */
public class MainApp extends Application {

    public static final int VERSION_MAJOR = 0;
    public static final int VERSION_MINOR = 0;
    public static final int VERSION_PATCH = 0;

    private static final Logger logger = LogManager.getLogger(MainApp.class);
    private static final Logger logger2 = LogManager.getLogger("Csv");

    protected Config config;
    protected StorageManager storageManager;
    protected ModelManager modelManager;
    protected SyncManager syncManager;
    protected UpdateManager updateManager;
    private MainController mainController;
    private ShortcutsManager shortcutsManager;

    public MainApp() {}

    protected Config getConfig() {
        return new Config();
    }

    @Override
    public void init() throws Exception {
        super.init();
        BrowserManager.initializeBrowser();
    }

    @Override
    public void start(Stage primaryStage) {
        setupComponents();
        mainController.start(primaryStage);
        updateManager.run();
        // initial load (precondition: mainController has been started.)
        EventManager.getInstance().post(new LoadDataRequestEvent(PrefsManager.getInstance().getSaveLocation()));
        syncManager.startSyncingData(config.updateInterval, config.simulateUnreliableNetwork);
    }

    protected void setupComponents() {
        config = getConfig();
        modelManager = new ModelManager();
        storageManager = new StorageManager(modelManager, PrefsManager.getInstance());
        mainController = new MainController(this, modelManager, config);
        syncManager = new SyncManager();

        shortcutsManager = new ShortcutsManager();

        updateManager = new UpdateManager();
        alertMissingDependencies();
    }

    private void alertMissingDependencies() {
        List<String> missingDependencies = updateManager.getMissingDependencies();

        if (missingDependencies.isEmpty()) {
            logger.info("All dependencies are present");
        } else {
            String message = "Missing dependencies:\n";
            for (String missingDependency : missingDependencies) {
                message += "- " + missingDependency + "\n";
            }
            logger.info(message.trim());

            mainController.showAlertDialogAndWait(Alert.AlertType.WARNING, "Missing Dependencies",
                    "There are missing dependencies. App may not work properly.",
                    message.trim());
        }
    }

    @Override
    public void stop() {
        mainController.getPrimaryStage().hide();
        mainController.releaseResourcesForAppTermination();
        updateManager.applyUpdate();
        shortcutsManager.clear();
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
