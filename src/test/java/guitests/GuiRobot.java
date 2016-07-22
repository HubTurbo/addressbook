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

    public GuiRobot push(KeyCode... keyCodes){
        return (GuiRobot) super.push(TestUtil.scrub(keyCodes));
    }

    public GuiRobot push(KeyCodeCombination keyCodeCombination){
        return (GuiRobot) super.push(TestUtil.scrub(keyCodeCombination));
    }

    public GuiRobot push(KeyBinding keyBinding){
        KeyCodeCombination keyCodeCombination = (KeyCodeCombination)keyBinding.getKeyCombination();
        return this.push(TestUtil.scrub(keyCodeCombination));
    }

    public GuiRobot press(KeyCode... keyCodes) {
        return (GuiRobot) super.press(TestUtil.scrub(keyCodes));
    }

    public GuiRobot release(KeyCode... keyCodes) {
        return (GuiRobot) super.release(TestUtil.scrub(keyCodes));
    }

    public GuiRobot type(KeyCode... keyCodes) {
        return (GuiRobot) super.type(TestUtil.scrub(keyCodes));
    }

    public void pushKeySequence(KeySequence keySequence) {
        push((KeyCodeCombination)keySequence.getKeyCombination());
        push((KeyCodeCombination)keySequence.getSecondKeyCombination());
    }

    @Override
    public GuiRobot clickOn(String query, MouseButton... buttons) {
        //Busy waiting implementation to fix issue when app window is not brought up to screen yet.
        int count = 0;
        while (count < 10) {
            try {
                return (GuiRobot) super.clickOn(query, buttons);
            } catch (FxRobotException e) {
                sleep(500);
                count++;
            }
        }
        return (GuiRobot) super.clickOn(query, buttons);
    }

    @Override
    public GuiRobot drag(String query, MouseButton... buttons) {
        //Busy waiting implementation to fix issue when app window is not brought up to screen yet.
        int count = 0;
        while (count < 10) {
            try {
                return (GuiRobot) super.drag(query, buttons);
            } catch (FxRobotException e) {
                count++;
                sleep(500);
            }
        }
        return (GuiRobot) super.drag(query, buttons);

    }
}
