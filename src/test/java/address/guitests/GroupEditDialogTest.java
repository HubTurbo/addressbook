package address.guitests;

import javafx.scene.control.TextField;
import org.junit.Test;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.hasText;

public class GroupEditDialogTest extends GuiTestBase {
    @Test
    public void groupEditDialogLoadTest() {
        clickOn("Groups").clickOn("New Group");

        verifyThat("#groupNameField", hasText(""));

        clickOn("Cancel").clickOn("Groups").clickOn("Manage Groups")
                .doubleClickOn(targetWindow("List of Contact Groups").lookup("friends").tryQuery().get());

        targetWindow("Edit Group");
        verifyThat("#groupNameField", hasText("friends"));
    }

    //TODO: verify that group list has been updated, currently no way to refresh group list
    @Test
    public void updateGroupTest() {
        clickOn("Groups").clickOn("Manage Groups")
                .doubleClickOn(targetWindow("List of Contact Groups").lookup("friends").tryQuery().get())
                .clickOn("#groupNameField").eraseText(7).write("changed group").clickOn("OK");
    }
}
