package address.guitests;

import address.TestApp;
import address.events.EventManager;
import address.keybindings.KeyBinding;
import address.keybindings.KeySequence;
import address.model.datatypes.ReadOnlyAddressBook;
import address.util.OsDetector;
import address.util.TestUtil;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        FxToolkit.setupApplication(() -> new TestApp(() -> getInitialData(), getDataFileLocation()));
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

    protected void pressKeyCombo(KeyCode... keys){
        pressKeyCombo(Arrays.asList(keys));
    }

    protected void pressKeyCombo(List<KeyCode> keys){
        keys.forEach(this::press);
        keys.forEach(this::release);
    }

    protected void pressKeyCombo(KeyCodeCombination combo){
        pressKeyCombo(getKeyCodes(combo));
    }

    protected void pressKeyCombo(KeyBinding kb){
        KeyCodeCombination keyCodeCombination = (KeyCodeCombination)kb.getKeyCombination();
        pressKeyCombo(getKeyCodes(keyCodeCombination));
    }

    private List<KeyCode> getKeyCodes(KeyCodeCombination combination) {
        List<KeyCode> keys = new ArrayList<>();
        if (combination.getAlt() == KeyCombination.ModifierValue.DOWN) {
            keys.add(KeyCode.ALT);
        }
        if (combination.getShift() == KeyCombination.ModifierValue.DOWN) {
            keys.add(KeyCode.SHIFT);
        }
        if (combination.getMeta() == KeyCombination.ModifierValue.DOWN) {
            keys.add(KeyCode.META);
        }
        if (combination.getControl() == KeyCombination.ModifierValue.DOWN) {
            keys.add(KeyCode.CONTROL);
        }
        if (combination.getShortcut() == KeyCombination.ModifierValue.DOWN) {
            //TODO: add back this hack (from HubTurbo cod)
            // Fix bug with internal method not having a proper code for SHORTCUT.
            // Dispatch manually based on platform.
//            if (PlatformSpecific.isOnMac()) {
//                keys.add(KeyCode.META);
//            } else {
                keys.add(KeyCode.CONTROL);
//            }
        }
        keys.add(combination.getCode());
        return keys;
    }

    public KeyCodeCombination shortcut(KeyCode keyCode) {
        return new KeyCodeCombination(keyCode, KeyCodeCombination.SHORTCUT_DOWN);
    }

    public FxRobot push(KeyCodeCombination keys) {
        return super.push(getPlatformSpecificKeyCombination(keys));
    }

    private KeyCodeCombination getPlatformSpecificKeyCombination(KeyCodeCombination keys) {
        if (keys.getShortcut() != KeyCodeCombination.ModifierValue.DOWN) return keys;
        KeyCodeCombination.Modifier shortcut = OsDetector.isOnMac() ? KeyCodeCombination.META_DOWN
                                                                    : KeyCodeCombination.CONTROL_DOWN;
        return new KeyCodeCombination(keys.getCode(), shortcut);
    }

    protected void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void pressKeySequence(KeySequence sequence) {
        pressKeyCombo((KeyCodeCombination)sequence.getKeyCombination());
        pressKeyCombo((KeyCodeCombination)sequence.getSecondKeyCombination());
    }

    protected void pressEsc() {
        pressKeyCombo(KeyCode.ESCAPE);
    }

    protected void pressEnter() {
        pressKeyCombo(KeyCode.ENTER);
    }
}
