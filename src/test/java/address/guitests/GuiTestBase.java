package address.guitests;

import address.TestApp;
import address.events.EventManager;
import address.keybindings.KeyBinding;
import address.keybindings.KeySequence;
import address.model.datatypes.ReadOnlyAddressBook;
import address.testutils.TestUtil;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;

import java.util.concurrent.TimeoutException;

public class GuiTestBase extends FxRobot {

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
        FxToolkit.setupApplication(() -> new TestApp(this::getInitialData, getDataFileLocation()));
        FxToolkit.showStage();
    }

    /**
     * Override this in child classes to set the initial data.
     * Return null to use the data in the file specified in {@link #getDataFileLocation()}
     */
    protected ReadOnlyAddressBook getInitialData() {
        return TestUtil.generateSampleAddressBook();
    }

    /**
     * Override this in child classes to set the data file location.
     * @return
     */
    protected String getDataFileLocation(){
        return TestApp.SAVE_LOCATION_FOR_TESTING;
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

    protected FxRobot push(KeyBinding keyBinding){
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

    protected void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void pushKeySequence(KeySequence keySequence) {
        push((KeyCodeCombination)keySequence.getKeyCombination());
        push((KeyCodeCombination)keySequence.getSecondKeyCombination());
    }

    public FxRobot clickOn(String query) {
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                clickOn(query);
                return this;
            } catch (Exception e) {
                System.out.println("Going to retry clicking " + query + ", retry count " + (i+1));
                if(i == maxRetries - 1) throw e;
                delay(500);
            }
        }
        return this;
    }

}
