package guitests.guihandles;

import address.controller.MainController;
import guitests.GuiRobot;
import javafx.stage.Stage;

/**
 * Provides a handle to the dialog used for adding a new tag.
 */
public class NewTagDialogHandle extends GuiHandle {

    private static final String TAG_NAME_FIELD_ID = "#tagNameField";

    public NewTagDialogHandle(GuiRobot guiRobot, Stage primaryStage) {
        super(guiRobot, primaryStage, MainController.DIALOG_TITLE_TAG_NEW);
    }

    public String getTagName() {
        return getTextFieldText(TAG_NAME_FIELD_ID);
    }

    public void enterTagName(String tagName) {
        typeTextField(TAG_NAME_FIELD_ID, tagName);
    }

    @Override
    public void pressEnter() {
        super.pressEnter();
        focusOnMainApp();
    }

    @Override
    protected void pressEsc() {
        super.pressEsc();
        focusOnMainApp();
    }

    @Override
    public void clickOk() {
        super.clickOk();
        focusOnMainApp();
    }

    @Override
    public void clickCancel() {
        super.clickCancel();
        focusOnMainApp();
    }
}
