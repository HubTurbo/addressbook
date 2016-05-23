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
}
