package address;

import address.model.ModelManager;
import address.keybindings.KeyBindingsManager;
import address.model.UserPrefs;
import address.storage.StorageManager;
import address.sync.SyncManager;
import address.ui.Ui;
import address.updater.UpdateManager;
import address.util.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * The main entry point to the application.
 */
public class MainApp extends Application {
    private static final AppLogger logger = LoggerManager.getLogger(MainApp.class);

    private static final int VERSION_MAJOR = 0;
    private static final int VERSION_MINOR = 0;
    private static final int VERSION_PATCH = 2;
    private static final boolean VERSION_EARLY_ACCESS = false;

    public static final Version VERSION = new Version(
            VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH, VERSION_EARLY_ACCESS);

    /**
     * Minimum Java Version Required
     *
     * Due to usage of ControlsFX 8.40.10, requires minimum Java version of 1.8.0_60.
     */
    public static final String REQUIRED_JAVA_VERSION = "1.8.0_60"; // update docs if this is changed

    protected StorageManager storageManager;
    protected ModelManager modelManager;
    protected SyncManager syncManager;
    protected UpdateManager updateManager;
    protected Ui ui;
    protected KeyBindingsManager keyBindingsManager;
    protected Config config;
    protected UserPrefs userPrefs;

    public MainApp() {}

    @Override
    public void init() throws Exception {
        logger.info("Initializing app ...");
        super.init();
        new DependencyChecker(REQUIRED_JAVA_VERSION, this::quit).verify();
        config = initConfig();
        userPrefs = initPrefs(config);
        initComponents(config, userPrefs);
    }

    protected Config initConfig() {
        Config config = StorageManager.getConfig();
        logger.info("Config successfully obtained from StorageManager");
        return config;
    }

    protected UserPrefs initPrefs(Config config) {
        UserPrefs userPrefs = StorageManager.getUserPrefs(config.getPrefsFileLocation());
        return userPrefs;
    }

    protected void initComponents(Config config, UserPrefs userPrefs) {
        LoggerManager.init(config);

        modelManager = new ModelManager(userPrefs);
        storageManager = new StorageManager(modelManager::resetData, config, userPrefs);
        ui = new Ui(this, modelManager, config);
        syncManager = new SyncManager(config);
        keyBindingsManager = new KeyBindingsManager();
        updateManager = new UpdateManager(VERSION);
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting application: {}", MainApp.VERSION);
        ui.start(primaryStage);
        updateManager.start();
        storageManager.start();
        syncManager.start();
    }

    @Override
    public void stop() {
        logger.info("Stopping application.");
        ui.stop();
        updateManager.stop();
        keyBindingsManager.stop();
        quit();
    }

    private void quit() {
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
