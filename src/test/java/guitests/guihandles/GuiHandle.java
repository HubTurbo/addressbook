package guitests.guihandles;


import address.model.ModelManager;
import guitests.GuiRobot;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.testfx.api.FxRobot;

import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;

/**
 * Base class for all GUI Handles used in testing.
 */
public class GuiHandle {
    protected final GuiRobot guiRobot;
    protected final Stage primaryStage;

    public GuiHandle(GuiRobot guiRobot, Stage primaryStage) {
        this.guiRobot = guiRobot;
        this.primaryStage = primaryStage;
    }

    /**
     * Creates an object of the specified GuiHandle child class.
     */
    public <T> T as(Class<? extends GuiHandle> clazz) {
        try {
            Constructor<?> ctor = clazz.getConstructor(GuiRobot.class, Stage.class);
            Object object = ctor.newInstance(new Object[] { guiRobot, primaryStage });
            return (T)object;
        } catch (Exception e) {
            throw new RuntimeException("Cannot create gui handle of type " + clazz.getName(), e);
        }
    }

    public FxRobot sleepForGracePeriod() {
        return guiRobot.sleep((ModelManager.GRACE_PERIOD_DURATION + 1), TimeUnit.SECONDS);
    }

    protected Node getNode(String query) {
        return guiRobot.lookup(query).tryQuery().get();
    }

    protected String getTextFieldText(String filedName) {
        return ((TextField) getNode(filedName)).getText();
    }

    /**
     * Sets the specified text field directly (as opposed to simulating the user typing).
     * @param textFieldId
     * @param newText
     */
    protected void setTextField(String textFieldId, String newText) {
        TextField textField = (TextField)getNode(textFieldId);
        textField.setText(newText);
    }

    /**
     * Simulates the user typing text in the given text field
     * @param textFieldId
     * @param newText
     */
    protected void typeTextField(String textFieldId, String newText) {
        guiRobot.clickOn(textFieldId)
                .push(KeyCode.SHORTCUT, KeyCode.A).eraseText(1)
                .write(newText)
        ;
    }

    public void pressEnter() {
        guiRobot.type(KeyCode.ENTER);
    }

    protected void pressEsc() {
        guiRobot.push(KeyCode.ESCAPE);
    }

    /**
     * Presses the button with the name 'Cancel' on it.
     */
    public void clickCancel() {
        guiRobot.clickOn("Cancel");
    }

    /**
     * Presses the button named 'OK'.
     */
    public void clickOk() {
        guiRobot.clickOn("OK");
    }

    public GuiHandle clickOn(String id) {
        guiRobot.clickOn(id);
        return this;
    }

    public GuiHandle rightClickOn(String id) {
        guiRobot.rightClickOn(id);
        return this;
    }

    /**
     * Dismisses the dialog by pressing Esc
     */
    public void dismiss() {
        pressEsc();
    }

    public void dissmissErrorMessage(String errorDialogTitle) {
        guiRobot.targetWindow(errorDialogTitle);
        clickOk();
    }

    protected String getTextFromLabel(String fieldId, Node parentNode) {
        return ((Label)guiRobot.from(parentNode).lookup(fieldId).tryQuery().get()).getText();
    }
}
