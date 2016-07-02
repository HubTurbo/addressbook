package address;

import address.model.UserPrefs;
import address.model.datatypes.ReadOnlyAddressBook;
import address.storage.StorageAddressBook;
import address.util.Config;
import address.testutils.TestUtil;

import java.util.function.Supplier;

public class TestApp extends MainApp {

    public static final String SAVE_LOCATION_FOR_TESTING = TestUtil.appendToSandboxPath("sampleData.xml");
    protected Supplier<ReadOnlyAddressBook> initialDataSupplier = TestUtil::generateSampleAddressBook;
    protected String saveFileLocation = SAVE_LOCATION_FOR_TESTING;


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

    public static void main(String[] args) {
        launch(args);
    }
}
