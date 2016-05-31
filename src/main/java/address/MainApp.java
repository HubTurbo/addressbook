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

import address.util.FileUtil;
import address.util.OsDetector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * The main entry point to the application.
 */
public class MainApp extends Application {

    public static final int VERSION_MAJOR = 1;
    public static final int VERSION_MINOR = 0;
    public static final int VERSION_PATCH = 0;

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
        checkDependencies();
        mainController.start(primaryStage);
        updateManager.run();
        // initial load (precondition: mainController has been started.)
        EventManager.getInstance().post(new LoadDataRequestEvent(PrefsManager.getInstance().getSaveLocation()));
        syncManager.startSyncingData(config.updateInterval, config.simulateUnreliableNetwork);
    }

    protected void checkDependencies() {
        Optional<String> classPath = getClassPathAttributeFromManifest();

        if (!classPath.isPresent()) {
            System.out.println("Class-path undefined, not running dependency check");
            return;
        }

        List<String> dependencies = new ArrayList<>(Arrays.asList(classPath.get().split("\\s+")));

        processDependenciesException(dependencies);

        alertMissingDependencies(dependencies);
    }

    /**
     * @return the format is space delimited list, e.g. "lib/1.jar lib/2.jar lib/etc.jar"
     */
    private Optional<String> getClassPathAttributeFromManifest() {
        Class mainAppClass = MainApp.class;
        String className = mainAppClass.getSimpleName() + ".class";
        String resourcePath = mainAppClass.getResource(className).toString();
        if (!resourcePath.startsWith("jar")) {
            System.out.println("Not from JAR, not running dependency check");
            return Optional.empty();
        }
        String manifestPath = resourcePath.substring(0, resourcePath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";

        Manifest manifest;

        try {
            manifest = new Manifest(new URL(manifestPath).openStream());
        } catch (IOException e) {
            System.out.println("Manifest can't be read, not running dependency check");
            e.printStackTrace();
            return Optional.empty();
        }

        Attributes attr = manifest.getMainAttributes();
        return Optional.of(attr.getValue("Class-path"));
    }

    private void processDependenciesException(List<String> dependencies) {
        List<String> windowsDependencies = new ArrayList<>();
        windowsDependencies.add("lib/jxbrowser-win-6.4.jar");
        List<String> macDependencies = new ArrayList<>();
        macDependencies.add("lib/jxbrowser-mac-6.4.jar");
        List<String> linux32Dependencies = new ArrayList<>();
        linux32Dependencies.add("lib/jxbrowser-linux32-6.4.jar");
        List<String> linux64Dependencies = new ArrayList<>();
        linux64Dependencies.add("lib/jxbrowser-linux64-6.4.jar");

        if (OsDetector.isOnWindows()) {
            dependencies.removeAll(macDependencies);
            dependencies.removeAll(linux32Dependencies);
            dependencies.removeAll(linux64Dependencies);
        } else if (OsDetector.isOnMac()) {
            dependencies.removeAll(windowsDependencies);
            dependencies.removeAll(linux32Dependencies);
            dependencies.removeAll(linux64Dependencies);
        } else if (OsDetector.isOn32BitsLinux()) {
            dependencies.removeAll(windowsDependencies);
            dependencies.removeAll(macDependencies);
            dependencies.removeAll(linux64Dependencies);
        } else if (OsDetector.isOn64BitsLinux()) {
            dependencies.removeAll(windowsDependencies);
            dependencies.removeAll(macDependencies);
            dependencies.removeAll(linux32Dependencies);
        }
    }

    private void alertMissingDependencies(List<String> dependencies) {
        List<String> missingDependencies = dependencies.stream()
                .filter(dependency -> !FileUtil.isFileExists(dependency)).collect(Collectors.toList());

        if (missingDependencies.isEmpty()) {
            System.out.println("All dependencies are present");
        } else {
            String message = "Missing dependencies:\n";
            for (String missingDependency : missingDependencies) {
                message += "- " + missingDependency + "\n";
            }
            System.out.println(message.trim());

            mainController.showAlertDialogAndWait(Alert.AlertType.WARNING, "Missing Dependencies",
                    "There are missing dependencies. App may not work properly.",
                    message.trim());
        }
    }

    protected void setupComponents() {
        config = getConfig();
        modelManager = new ModelManager();
        storageManager = new StorageManager(modelManager);
        mainController = new MainController(this, modelManager, config);
        syncManager = new SyncManager();

        shortcutsManager = new ShortcutsManager();

        updateManager = new UpdateManager();

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
