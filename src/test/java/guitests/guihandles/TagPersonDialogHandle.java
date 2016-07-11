package guitests.guihandles;

import guitests.GuiRobot;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * Provides a handle for the dialog used for tagging a person.
 */
public class TagPersonDialogHandle extends GuiHandle {

    private String tagSearchFieldId = "#tagSearch";

    public TagPersonDialogHandle(GuiRobot guiRobot, Stage primaryStage) {
        super(guiRobot, primaryStage);
    }

    public TagPersonDialogHandle enterSearchQuery(String queryText) {
        typeTextField(tagSearchFieldId, queryText);
        return this;
    }

    public TagPersonDialogHandle acceptSuggestedTag() {
        guiRobot.type(KeyCode.SPACE);
        return this;
    }

    public void close() {
        super.pressEnter();
        guiRobot.sleep(200); // wait for closing animation
    }
}
