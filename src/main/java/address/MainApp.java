package address;

import address.model.ModelManager;
import address.keybindings.KeyBindingsManager;
import address.model.UserPrefs;
import address.storage.StorageManager;
import address.sync.RemoteManager;
import address.sync.SyncManager;
import address.sync.cloud.CloudSimulator;
import address.ui.Ui;
import address.updater.UpdateProgressNotifier;
import address.updater.UpdaterUpgrader;
import commons.UpdateInformationNotifier;
import commons.Version;
import updater.Updater;
import address.util.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

/**
 * The main entry point to the application.
 */
public class MainApp extends Application {
    private static final AppLogger logger = LoggerManager.getLogger(MainApp.class);

    private static final int VERSION_MAJOR = 1;
    private static final int VERSION_MINOR = 4;
    private static final int VERSION_PATCH = 0;
    private static final boolean VERSION_EARLY_ACCESS = true;

    public static final commons.Version VERSION = new commons.Version(
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
    protected Updater updater;
    protected RemoteManager remoteManager;
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
        Map<String, String> applicationParameters = getParameters().getNamed();
        config = initConfig(applicationParameters.get("config"));
        userPrefs = initPrefs(config);
        initComponents(config, userPrefs);
    }

    protected Config initConfig(String configFilePath) {
        return StorageManager.getConfig(configFilePath);
    }

    protected UserPrefs initPrefs(Config config) {
        return StorageManager.getUserPrefs(config.getPrefsFileLocation());
    }

    private void initComponents(Config config, UserPrefs userPrefs) {
        LoggerManager.init(config);

        modelManager = initModelManager(config);
        storageManager = initStorageManager(modelManager, config, userPrefs);
        ui = initUi(config, modelManager);
        remoteManager = initRemoteManager(config);
        syncManager = initSyncManager(remoteManager, config);
        keyBindingsManager = initKeyBindingsManager();
        updater = initUpdater(VERSION);
    }

    protected Updater initUpdater(Version version) {
        return new Updater(version);
    }

    protected KeyBindingsManager initKeyBindingsManager() {
        return new KeyBindingsManager();
    }

    protected RemoteManager initRemoteManager(Config config) {
        return new RemoteManager(new CloudSimulator(config));
    }

    protected SyncManager initSyncManager(RemoteManager remoteManager, Config config) {
        return new SyncManager(remoteManager, config, config.getAddressBookName());
    }

    protected Ui initUi(Config config, ModelManager modelManager) {
        return new Ui(this, modelManager, config, userPrefs);
    }

    protected StorageManager initStorageManager(ModelManager modelManager, Config config, UserPrefs userPrefs) {
        return new StorageManager(modelManager::resetData, modelManager::getDefaultAddressBook, config, userPrefs);
    }

    protected ModelManager initModelManager(Config config) {
        return new ModelManager(config);
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting application: {}", MainApp.VERSION);
        ui.start(primaryStage);
        if (ManifestFileReader.isRunFromJar()) {
            updater.start(getUpdateInformationNotifier(ui));
        } else {
            ui.getUpdateProgressNotifier().sendStatusFinished("Developer environment; not running updater");
        }
        storageManager.start();
        syncManager.start();
    }

    protected UpdateInformationNotifier getUpdateInformationNotifier(Ui ui) {
        UpdateProgressNotifier updateProgressNotifier = ui.getUpdateProgressNotifier();
        return new UpdateInformationNotifier(
                updateProgressNotifier::sendStatusFinished,
                updateProgressNotifier::sendStatusFailed,
                updateProgressNotifier::sendStatusInProgress,
                (upgradeUpdater) -> {
                    UpdaterUpgrader updaterUpgrader = new UpdaterUpgrader(upgradeUpdater);
                    try {
                        updaterUpgrader.upgradeUpdater();
                    } catch (IOException e) {
                        logger.warn("Error upgrading updater: {}", e);
                    }
                }
        );
    }

    @Override
    public void stop() {
        logger.info("Stopping application.");
        ui.stop();
        storageManager.savePrefsToFile(userPrefs);
        syncManager.stop();
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
