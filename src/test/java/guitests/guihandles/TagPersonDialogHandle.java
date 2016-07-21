package guitests.guihandles;

import address.controller.MainController;
import guitests.GuiRobot;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * Provides a handle for the dialog used for tagging a person.
 */
public class TagPersonDialogHandle extends GuiHandle {

    private static final String TAG_SEARCH_FIELD_ID = "#tagSearch";

    public TagPersonDialogHandle(GuiRobot guiRobot, Stage primaryStage) {
        super(guiRobot, primaryStage, MainController.STAGE_TITLE_TAG_SELECTION);
    }

    public TagPersonDialogHandle enterSearchQuery(String queryText) {
        typeTextField(TAG_SEARCH_FIELD_ID, queryText);
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
