package guitests;

import address.keybindings.KeyBinding;
import address.keybindings.KeySequence;
import address.testutil.TestUtil;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseButton;
import org.testfx.api.FxRobot;
import org.testfx.api.FxRobotException;

/**
 * Robot used to simulate user actions on the GUI.
 * Extends {@link FxRobot} by adding some customized functionality and workarounds.
 */
public class GuiRobot extends FxRobot {

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

    @Override
    public FxRobot clickOn(String query, MouseButton... buttons) {
        //Busy waiting implementation to fix issue when app window is not brought up to screen yet.
        int count = 0;
        while (count < 10) {
            try {
                return super.clickOn(query, buttons);
            } catch (FxRobotException e) {
                sleep(500);
                count++;
            }
        }
        return super.clickOn(query, buttons);
    }

    @Override
    public FxRobot drag(String query, MouseButton... buttons) {
        //Busy waiting implementation to fix issue when app window is not brought up to screen yet.
        int count = 0;
        while (count < 10) {
            try {
                return super.drag(query, buttons);
            } catch (FxRobotException e) {
                count++;
                sleep(500);
            }
        }
        return super.drag(query, buttons);

    }
}
