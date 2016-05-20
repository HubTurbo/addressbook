package address.guitests;

import javafx.scene.control.TextField;
import org.junit.Test;

import static org.testfx.api.FxAssert.verifyThat;

public class GroupEditDialogTest extends GuiTestBase {
    @Test
    public void groupEditDialogLoadTest() {
        clickOn("Groups").clickOn("New Group");

        //findStageByTitle("Edit Group");
        verifyThat("#groupNameField", (TextField t) -> t.getText().equals(""));

        clickOn("Cancel").clickOn("Groups").clickOn("Manage Groups")

        // to be used after we can load from a custom data file from config
        .doubleClickOn(getWindowNode("List of Contact Groups").lookup("friends").tryQuery().get());

        findStageByTitle("Edit Group");
        verifyThat("#groupNameField", (TextField t) -> t.getText().equals("friends"));
    }
}
