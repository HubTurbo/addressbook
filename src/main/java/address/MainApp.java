package address;

import address.browser.BrowserManager;

import address.controller.MainController;
import address.events.EventManager;
import address.events.LoadDataRequestEvent;
import address.model.ModelManager;
import address.keybindings.KeyBindingsManager;
import address.prefs.PrefsManager;
import address.storage.StorageManager;
import address.sync.SyncManager;
import address.updater.UpdateManager;
import address.util.AppLogger;
import address.util.Config;

import address.util.LoggerManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.List;

/**
 * The main entry point to the application.
 */
public class MainApp extends Application {

    public static final int VERSION_MAJOR = 0;
    public static final int VERSION_MINOR = 0;
    public static final int VERSION_PATCH = 1;

    private static final AppLogger logger = LoggerManager.getLogger(MainApp.class);

    protected StorageManager storageManager;
    protected ModelManager modelManager;
    protected SyncManager syncManager;
    protected UpdateManager updateManager;
    private MainController mainController;
    private KeyBindingsManager keyBindingsManager;

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
        logger.info("Starting application: V{}.{}.{}", VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH);
        setupComponents();
        mainController.start(primaryStage);
        updateManager.run();
        // initial load (precondition: mainController has been started.)
        EventManager.getInstance().post(new LoadDataRequestEvent(PrefsManager.getInstance().getSaveLocation()));
        syncManager.startSyncingData(Config.getConfig().updateInterval, Config.getConfig().simulateUnreliableNetwork);
    }

    protected void setupComponents() {
        Config.setConfig(getConfig());
        modelManager = new ModelManager();
        storageManager = new StorageManager(modelManager, PrefsManager.getInstance());
        mainController = new MainController(this, modelManager);
        syncManager = new SyncManager();

        keyBindingsManager = new KeyBindingsManager();

        updateManager = new UpdateManager();
        alertMissingDependencies();
    }

    private void alertMissingDependencies() {
        List<String> missingDependencies = updateManager.getMissingDependencies();

        if (missingDependencies.isEmpty()) {
            logger.info("All dependencies are present");
        } else {
            StringBuilder message = new StringBuilder("Missing dependencies:\n");
            for (String missingDependency : missingDependencies) {
                message.append("- " + missingDependency + "\n");
            }
            String missingDependenciesMessage = message.toString().trim();
            logger.warn(missingDependenciesMessage);

            mainController.showAlertDialogAndWait(Alert.AlertType.WARNING, "Missing Dependencies",
                    "There are missing dependencies. App may not work properly.",
                    missingDependenciesMessage);
        }
    }

    @Override
    public void stop() {
        mainController.getPrimaryStage().hide();
        mainController.releaseResourcesForAppTermination();
        updateManager.applyUpdate();
        keyBindingsManager.clear();
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
