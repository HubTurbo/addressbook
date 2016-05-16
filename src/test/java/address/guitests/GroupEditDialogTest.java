package address.guitests;

import javafx.scene.control.TextField;
import org.junit.Test;

import static org.testfx.api.FxAssert.verifyThat;

public class GroupEditDialogTest extends GuiTestBase {
    @Test
    public void groupEditDialogLoadTest() {
        clickOn("Groups").clickOn("New Group");

        findStageByTitle("Edit Group");
        verifyThat("#groupNameField", (TextField t) -> t.getText().equals(""));

        clickOn("Cancel").clickOn("Groups").clickOn("Manage Groups")
        .doubleClickOn(getWindowNode("List of Contact Groups").lookup("friends").tryQuery().get());

        findStageByTitle("Edit Group");
        verifyThat("#groupNameField", (TextField t) -> t.getText().equals("friends"));
    }
}
