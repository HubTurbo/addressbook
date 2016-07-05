package address;

import address.model.UserPrefs;
import address.model.datatypes.AddressBook;
import address.model.datatypes.ReadOnlyAddressBook;
import address.storage.StorageAddressBook;
import address.sync.RemoteManager;
import address.sync.RemoteService;
import address.sync.cloud.CloudManipulator;
import address.sync.cloud.IRemote;
import address.util.Config;
import address.util.TestUtil;
import javafx.stage.Stage;

import java.util.function.Supplier;

public class TestApp extends MainApp {

    public static final String SAVE_LOCATION_FOR_TESTING = TestUtil.appendToSandboxPath("sampleData.xml");
    protected Supplier<ReadOnlyAddressBook> initialDataSupplier = TestUtil::generateSampleAddressBook;
    protected String saveFileLocation = SAVE_LOCATION_FOR_TESTING;
    protected CloudManipulator remote;

    public TestApp() {

    }

    public TestApp(Supplier<ReadOnlyAddressBook> initialDataSupplier, String saveFileLocation) {
        super();
        this.initialDataSupplier = initialDataSupplier;
        this.saveFileLocation = saveFileLocation;

        //If some intial data has been provided, write those to the file
        if (initialDataSupplier.get() != null) {
            TestUtil.createDataFileWithData(
                    new StorageAddressBook(this.initialDataSupplier.get()),
                    this.saveFileLocation);
        }
    }

    @Override
    protected Config initConfig() {
        Config config = super.initConfig();
        config.appTitle = "Test App";
        return config;
    }

    @Override
    protected UserPrefs initPrefs(Config config) {
        UserPrefs userPrefs = super.initPrefs(config);
        userPrefs.setSaveLocation(saveFileLocation);
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
