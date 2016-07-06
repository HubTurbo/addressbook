package guitests;

import address.TestApp;
import address.events.EventManager;
import address.keybindings.KeyBinding;
import address.keybindings.KeySequence;
import address.model.datatypes.ReadOnlyAddressBook;
import address.sync.cloud.model.CloudAddressBook;
import address.util.TestUtil;
import guitests.guihandles.MainGuiHandle;
import guitests.guihandles.MainMenuHandle;
import guitests.guihandles.PersonListPanelHandle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;

import java.util.concurrent.TimeoutException;

public class GuiTestBase extends FxRobot {

    /* Handles to GUI elements present at the start up are created in advance
     *   for easy access from child classes.
     */
    protected MainGuiHandle mainGui = new MainGuiHandle(this);
    protected MainMenuHandle mainMenu = mainGui.getMainMenu();
    protected PersonListPanelHandle personListPanel = mainGui.getPersonListPanel();


    @BeforeClass
    public static void setupSpec() {
        try {
            FxToolkit.registerPrimaryStage();
            FxToolkit.hideStage();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setup() throws Exception {
        EventManager.clearSubscribers();
        FxToolkit.setupApplication(() -> new TestApp(this::getInitialData, this::getInitialCloudData,
                                                     getDataFileLocation(), getCloudFileLocation()));
        FxToolkit.showStage();
    }

    /**
     * Override this in child classes to set the initial data.
     * Return null to use the data in the file specified in {@link #getDataFileLocation()}
     */
    protected ReadOnlyAddressBook getInitialData() {
        return TestUtil.generateSampleAddressBook();
    }

    protected CloudAddressBook getInitialCloudData() {
        return TestUtil.generateSampleCloudAddressBook();
    }

    /**
     * Override this in child classes to set the data file location.
     * @return
     */
    protected String getDataFileLocation() {
        return TestApp.SAVE_LOCATION_FOR_TESTING;
    }

    /**
     * Override this in child classes to set the data file location.
     * @return
     */
    protected String getCloudFileLocation() {
        return TestApp.CLOUD_LOCATION_FOR_TESTING;
    }

    @After
    public void cleanup() throws TimeoutException {
        FxToolkit.cleanupStages();
    }

    public FxRobot push(KeyCode... keyCodes){
        return super.push(TestUtil.scrub(keyCodes));
    }

    public FxRobot push(KeyCodeCombination keyCodeCombination){
        return super.push(TestUtil.scrub(keyCodeCombination));
    }

    public FxRobot push(KeyBinding keyBinding){
        KeyCodeCombination keyCodeCombination = (KeyCodeCombination)keyBinding.getKeyCombination();
        return this.push(TestUtil.scrub(keyCodeCombination));
    }

    public FxRobot press(KeyCode... keyCodes) {
        return super.press(TestUtil.scrub(keyCodes));
    }

    public FxRobot release(KeyCode... keyCodes) {
        return super.release(TestUtil.scrub(keyCodes));
    }

    public FxRobot type(KeyCode... keyCodes) {
        return super.type(TestUtil.scrub(keyCodes));
    }

    public void pushKeySequence(KeySequence keySequence) {
        push((KeyCodeCombination)keySequence.getKeyCombination());
        push((KeyCodeCombination)keySequence.getSecondKeyCombination());
    }


}
