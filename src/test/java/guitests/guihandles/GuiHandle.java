package guitests.guihandles;


import guitests.GuiTestBase;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.lang.reflect.Constructor;

/**
 * Base class for all GUI Handles used in testing.
 */
public class GuiHandle {
    protected final GuiTestBase guiTestBase;

    public GuiHandle(GuiTestBase guiTestBase){
        this.guiTestBase = guiTestBase;
    }

    /**
     * Creates an object of the specified GuiHandle child class.
     */
    public <T> T as(Class<? extends GuiHandle> clazz) {
        try {
            Constructor<?> ctor = clazz.getConstructor(GuiTestBase.class);
            Object object = ctor.newInstance(new Object[] { guiTestBase });
            return (T)object;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot create gui handle of type " + clazz.getName());
        }
    }

    protected Node getNode(String query) {
        return guiTestBase.lookup(query).tryQuery().get();
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
        guiTestBase.clickOn(textFieldId)
                .push(KeyCode.SHORTCUT, KeyCode.A).eraseText(1)
                .write(newText)
        ;
    }

    public void pressEnter() {
        guiTestBase.type(KeyCode.ENTER);
    }

    protected void pressEsc() {
        guiTestBase.push(KeyCode.ESCAPE);
    }

    /**
     * Presses the button with the name 'Cancel' on it.
     */
    public void clickCancel() {
        guiTestBase.clickOn("Cancel");
    }

    /**
     * Presses the button named 'OK'.
     */
    public void clickOk() {
        guiTestBase.clickOn("OK");
    }

    public GuiHandle clickOn(String id) {
        guiTestBase.clickOn(id);
        return this;
    }

    public GuiHandle rightClickOn(String id) {
        guiTestBase.rightClickOn(id);
        return this;
    }

    /**
     * Dismisses the dialog by pressing Esc
     */
    public void dismiss() {
        pressEsc();
    }
}
