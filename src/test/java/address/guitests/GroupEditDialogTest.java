package address.guitests;

import javafx.scene.control.TextField;
import org.junit.Test;

import static org.testfx.api.FxAssert.verifyThat;

public class GroupEditDialogTest extends GuiTestBase {
    @Test
    public void groupEditDialogLoadTest() {
        // this test currently assumes that the default data file exists
        clickOn("File").clickOn("Append Sample Data")
                .clickOn("Groups").clickOn("New Group");

        verifyThat("#groupNameField", (TextField t) -> t.getText().equals(""));

        clickOn("Cancel").clickOn("Groups").clickOn("Manage Groups")
                .doubleClickOn(targetWindow("List of Contact Groups").lookup("friends").tryQuery().get());

        targetWindow("Edit Group");
        verifyThat("#groupNameField", (TextField t) -> t.getText().equals("friends"));
    }
}
