package address;

import address.browser.BrowserManager;

import address.controller.MainController;
import address.model.ModelManager;
import address.keybindings.KeyBindingsManager;
import address.prefs.PrefsManager;
import address.prefs.UserPrefs;
import address.storage.StorageManager;
import address.sync.SyncManager;
import address.updater.UpdateManager;
import address.util.AppLogger;
import address.util.Config;

import address.util.LoggerManager;
import address.util.Version;
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
    public static final int VERSION_PATCH = 2;
    public static final boolean IS_EARLY_ACCESS = false;
    //TODO: encapsulate these into a Version object?

    private static final AppLogger logger = LoggerManager.getLogger(MainApp.class);

    protected StorageManager storageManager;
    protected ModelManager modelManager;
    protected SyncManager syncManager;
    protected UpdateManager updateManager;
    protected MainController mainController;
    protected KeyBindingsManager keyBindingsManager;

    public MainApp() {}

    @Override
    public void init() throws Exception {
        logger.info("Initializing app ...");
        super.init();
        initConfig();
        Config.setConfig(Config.getConfig());
        initPrefs();
        BrowserManager.initializeBrowser();
        //TODO: should this be here? looks out of place
        initComponents();
    }

    protected void initConfig() {
        // For sub classes to override
    }

    protected void initPrefs() {
        // For sub classes to override
    }

    protected void initComponents() {
        modelManager = new ModelManager();
        storageManager = new StorageManager(modelManager, PrefsManager.getInstance().getPrefs());
        mainController = new MainController(this, modelManager);
        syncManager = new SyncManager();
        keyBindingsManager = new KeyBindingsManager();
        updateManager = new UpdateManager();
        alertMissingDependencies();
        //TODO: should this be here? looks out of place
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting application: {}", Version.getCurrentVersion());
        mainController.start(primaryStage);
        updateManager.start();
        storageManager.start();
        syncManager.start(Config.getConfig().updateInterval);
    }

    //TODO: this method is out of place
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
        logger.info("Stopping application.");
        mainController.stop();
        updateManager.stop();
        keyBindingsManager.stop();
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
