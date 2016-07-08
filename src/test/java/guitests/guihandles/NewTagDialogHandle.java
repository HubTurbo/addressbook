package guitests.guihandles;

import guitests.GuiRobot;
import javafx.stage.Stage;

/**
 * Provides a handle to the dialog used for adding a new tag.
 */
public class NewTagDialogHandle extends GuiHandle {

    private String tagNameFieldId = "#tagNameField";

    public NewTagDialogHandle(GuiRobot guiRobot, Stage primaryStage) {
        super(guiRobot, primaryStage);
    }

    public String getTagName() {
        return getTextFieldText(tagNameFieldId);
    }

    public void enterTagName(String tagName) {
        typeTextField(tagNameFieldId, tagName);
    }

}
