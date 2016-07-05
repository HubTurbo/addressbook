package guitests.guihandles;

import guitests.GuiTestBase;
import javafx.scene.input.KeyCode;

/**
 * Provides a handle for the dialog used for tagging a person.
 */
public class TagPersonDialogHandle extends GuiHandle {

    private String tagSearchFieldId = "#tagSearch";

    public TagPersonDialogHandle(GuiTestBase guiTestBase) {
        super(guiTestBase);
    }

    public TagPersonDialogHandle enterSearchQuery(String queryText) {
        typeTextField(tagSearchFieldId, queryText);
        return this;
    }

    public TagPersonDialogHandle acceptSuggestedTag() {
        guiTestBase.type(KeyCode.SPACE);
        return this;
    }

    public void close() {
        super.pressEnter();
        guiTestBase.sleep(200); // wait for closing animation
    }
}
