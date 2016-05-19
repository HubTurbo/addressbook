package address;

import address.model.AddressBook;
import com.teamdev.jxbrowser.chromium.BrowserCore;
import com.teamdev.jxbrowser.chromium.LoggerProvider;
import com.teamdev.jxbrowser.chromium.internal.Environment;

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
import javafx.stage.Stage;

import java.util.logging.Level;

/**
 * The main entry point to the application.
 */
public class MainApp extends Application {

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
        if (Environment.isMac()) {
            BrowserCore.initialize();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        LoggerProvider.setLevel(Level.SEVERE);
        setupComponents();
        mainController.start(primaryStage);

        // initial load (requires mainController to be started.
        EventManager.getInstance().post(new LoadDataRequestEvent(PrefsManager.getInstance().getSaveLocation()));
        syncManager.startSyncingData(config.updateInterval, config.simulateUnreliableNetwork);
    }

    protected void setupComponents() {
        config = getConfig();
        modelManager = new ModelManager(new AddressBook()); // empty model first
        storageManager = new StorageManager(modelManager);
        mainController = new MainController(this, modelManager, config);
        syncManager = new SyncManager();

        shortcutsManager = new ShortcutsManager();

        updateManager = new UpdateManager();
        updateManager.run();
    }

    @Override
    public void stop() {
        mainController.getPrimaryStage().hide();
        mainController.releaseResourcesForAppTermination();
        updateManager.applyUpdate();
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
