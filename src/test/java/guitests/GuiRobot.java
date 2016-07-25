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
        return (GuiRobot) super.clickOn(query, buttons);
    }

    @Override
    public GuiRobot drag(String query, MouseButton... buttons) {
        return (GuiRobot) super.drag(query, buttons);
    }
}
