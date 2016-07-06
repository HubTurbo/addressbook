package address;

import address.model.UserPrefs;
import address.model.datatypes.ReadOnlyAddressBook;
import address.storage.StorageAddressBook;
import address.sync.RemoteManager;
import address.sync.cloud.CloudManipulator;
import address.sync.cloud.model.CloudAddressBook;
import address.util.Config;
import address.util.TestUtil;
import javafx.stage.Stage;

import java.util.function.Supplier;

public class TestApp extends MainApp {

    public static final String SAVE_LOCATION_FOR_TESTING = TestUtil.appendToSandboxPath("sampleData.xml");
    public static final String DEFAULT_CLOUD_LOCATION_FOR_TESTING = TestUtil.appendToSandboxPath("sampleCloudData.xml");
    protected Supplier<ReadOnlyAddressBook> initialDataSupplier = () -> null;
    protected Supplier<CloudAddressBook> initalCloudDataSupplier = () -> null;
    protected String saveFileLocation = SAVE_LOCATION_FOR_TESTING;
    protected CloudManipulator remote;

    public TestApp() {
    }

    public TestApp(Supplier<ReadOnlyAddressBook> initialDataSupplier, String saveFileLocation,
                   Supplier<CloudAddressBook> initialCloudDataSupplier) {
        super();
        this.initialDataSupplier = initialDataSupplier;
        this.initalCloudDataSupplier = initialCloudDataSupplier;
        this.saveFileLocation = saveFileLocation;

        //If some intial data has been provided, write those to the file
        if (initialDataSupplier.get() != null) {
            TestUtil.createDataFileWithData(
                    new StorageAddressBook(this.initialDataSupplier.get()),
                    this.saveFileLocation);
        }
    }

    @Override
    protected Config initConfig(String configFilePath) {
        Config config = super.initConfig(configFilePath);
        config.appTitle = "Test App";
        config.setLocalDataFilePath(saveFileLocation);
        // Use default cloud test data if no data is supplied
        if (initalCloudDataSupplier.get() == null) config.setCloudDataFilePath(DEFAULT_CLOUD_LOCATION_FOR_TESTING);
        return config;
    }

    @Override
    protected UserPrefs initPrefs(Config config) {
        UserPrefs userPrefs = super.initPrefs(config);
        return userPrefs;
    }

    @Override
    protected RemoteManager initRemoteManager(Config config) {
        if (initalCloudDataSupplier.get() == null) {
            remote = new CloudManipulator(config);
        } else {
            remote = new CloudManipulator(config, initalCloudDataSupplier.get());
        }
        return new RemoteManager(remote);
    }

    @Override
    public void start(Stage primaryStage) {
        ui.start(primaryStage);
        updateManager.start();
        storageManager.start();
        syncManager.start();
        remote.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
