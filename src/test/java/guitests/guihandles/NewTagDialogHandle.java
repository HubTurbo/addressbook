package guitests.guihandles;

import guitests.GuiRobot;
import javafx.stage.Stage;

/**
 * Provides a handle to the dialog used for adding a new tag.
 */
public class NewTagDialogHandle extends GuiHandle {

    private static final String TAG_NAME_FIELD_ID = "#tagNameField";

    public NewTagDialogHandle(GuiRobot guiRobot, Stage primaryStage) {
        super(guiRobot, primaryStage);
    }

    public String getTagName() {
        return getTextFieldText(TAG_NAME_FIELD_ID);
    }

    public void enterTagName(String tagName) {
        typeTextField(TAG_NAME_FIELD_ID, tagName);
    }

}
