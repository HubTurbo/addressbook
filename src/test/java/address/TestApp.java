package address;

import address.controller.UiEx;
import address.model.ModelManager;
import address.model.UserPrefs;
import address.model.datatypes.ReadOnlyAddressBook;
import address.storage.StorageAddressBook;
import address.ui.Ui;
import address.util.Config;
import address.util.TestUtil;

import java.util.function.Supplier;

public class TestApp extends MainApp {

    public static final String SAVE_LOCATION_FOR_TESTING = TestUtil.appendToSandboxPath("sampleData.xml");
    protected Supplier<ReadOnlyAddressBook> initialDataSupplier = TestUtil::generateSampleAddressBook;
    protected String saveFileLocation = SAVE_LOCATION_FOR_TESTING;
    private UiEx uiEx;


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
    protected Ui createUi(MainApp mainApp, ModelManager modelManager, Config config) {
        this.uiEx = new UiEx(mainApp, modelManager, config);
        return uiEx;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public boolean listContains(String firstName, String lastName) {
        return uiEx.getDisplayedPersonList()
                .stream().anyMatch(p -> p.isSameName(firstName, lastName));
    }

    public boolean isSelectedPerson(String firstName, String lastName) {
        return uiEx.getSelectedPersons().get(0).isSameName(firstName, lastName);
    }

    public boolean isFocusedPerson(String firstName, String lastName) {
        return uiEx.getFocusedPerson().isSameName(firstName, lastName);
    }
}
