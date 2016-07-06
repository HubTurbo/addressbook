package address;

import address.model.UserPrefs;
import address.model.datatypes.AddressBook;
import address.model.datatypes.ReadOnlyAddressBook;
import address.storage.StorageAddressBook;
import address.sync.RemoteManager;
import address.sync.RemoteService;
import address.sync.cloud.CloudManipulator;
import address.sync.cloud.IRemote;
import address.sync.cloud.model.CloudAddressBook;
import address.util.Config;
import address.util.TestUtil;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.function.Supplier;

public class TestApp extends MainApp {

    public static final String SAVE_LOCATION_FOR_TESTING = TestUtil.appendToSandboxPath("sampleData.xml");
    public static final String CLOUD_LOCATION_FOR_TESTING = TestUtil.appendToSandboxPath("sampleCloudData.xml");
    protected Supplier<ReadOnlyAddressBook> initialDataSupplier = TestUtil::generateSampleAddressBook;
    protected Supplier<CloudAddressBook> initalCloudDataSupplier = TestUtil::generateSampleCloudAddressBook;
    protected String saveFileLocation = SAVE_LOCATION_FOR_TESTING;
    protected String cloudFileLocation = CLOUD_LOCATION_FOR_TESTING;
    protected CloudManipulator remote;

    public TestApp() {
    }

    public TestApp(Supplier<ReadOnlyAddressBook> initialDataSupplier, Supplier<CloudAddressBook> initialCloudDataSupplier,
                   String saveFileLocation, String cloudFileLocation) {
        super();
        this.initialDataSupplier = initialDataSupplier;
        this.initalCloudDataSupplier = initialCloudDataSupplier;
        this.saveFileLocation = saveFileLocation;
        this.cloudFileLocation = cloudFileLocation;

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
        config.setCloudDataFilePath(cloudFileLocation);
        return config;
    }

    @Override
    protected UserPrefs initPrefs(Config config) {
        UserPrefs userPrefs = super.initPrefs(config);
        return userPrefs;
    }

    @Override
    protected RemoteManager initRemoteManager(Config config) {
        remote = new CloudManipulator(config);
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
