package guitests;

import address.TestApp;
import address.events.EventManager;
import address.model.datatypes.AddressBook;
import address.sync.cloud.model.CloudAddressBook;
import address.testutil.ScreenShotRule;
import address.testutil.TypicalTestData;
import address.testutil.TestUtil;
import address.util.Config;
import guitests.guihandles.HeaderStatusBarHandle;
import guitests.guihandles.MainGuiHandle;
import guitests.guihandles.MainMenuHandle;
import guitests.guihandles.PersonListPanelHandle;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.loadui.testfx.GuiTest;
import org.testfx.api.FxToolkit;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GuiTestBase {

    @Rule
    public ScreenShotRule screenShotRule = new ScreenShotRule();

    @Rule
    public TestName name = new TestName();

    TestApp testApp;

    /* Handles to GUI elements present at the start up are created in advance
     *   for easy access from child classes.
     */
    protected MainGuiHandle mainGui;
    protected MainMenuHandle mainMenu;
    protected PersonListPanelHandle personListPanel;
    protected HeaderStatusBarHandle statusBar;
    protected TypicalTestData td = new TypicalTestData();
    private Stage stage;


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
        FxToolkit.setupStage((stage) -> {
            mainGui = new MainGuiHandle(new GuiRobot(), stage);
            mainMenu = mainGui.getMainMenu();
            personListPanel = mainGui.getPersonListPanel();
            statusBar = mainGui.getStatusBar();
            this.stage = stage;
        });
        EventManager.clearSubscribers();
        testApp = (TestApp) FxToolkit.setupApplication(() -> new TestApp(this::getInitialData, getDataFileLocation(),
                                                     this::selectFromInitialCloudData));
        FxToolkit.showStage();
        while(!stage.isShowing());
        mainGui.focusOnMainApp();
    }

    /**
     * Override this in child classes to set the initial local data.
     * Return null to use the data in the file specified in {@link #getDataFileLocation()}
     */
    protected AddressBook getInitialData() {
        return TestUtil.generateSampleAddressBook();
    }

    private CloudAddressBook selectFromInitialCloudData() {
        return getInitialCloudData() == null
            ?  TestUtil.generateCloudAddressBook(getInitialData())
            : getInitialCloudData();
    }

    /**
     * Override this in child classes to set the initial cloud data.
     * If not overridden, cloud data will be the same as local data.
     */
    protected CloudAddressBook getInitialCloudData() {
        return null;
    }

    /**
     * Override this in child classes to set the data file location.
     * @return
     */
    protected String getDataFileLocation() {
        return TestApp.SAVE_LOCATION_FOR_TESTING;
    }

    public Config getTestingConfig() {
        return testApp.getTestingConfig();
    }

    @After
    public void cleanup() throws TimeoutException {
        File file = GuiTest.captureScreenshot();
        TestUtil.renameFile(file, this.getClass().getName() + name.getMethodName() + ".png");
        FxToolkit.cleanupStages();
        testApp.deregisterHotKeys();
    }

    public void sleep(long duration, TimeUnit timeunit) {
        mainGui.sleep(duration, timeunit);
    }

}
