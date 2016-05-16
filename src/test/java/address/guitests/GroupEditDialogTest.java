package address.guitests;

import address.UITest;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GroupEditDialogTest extends UITest {
    @Test
    public void groupEditDialogLoadTest() {
        clickOn("Groups").clickOn("New Group");
        assertTrue(findStageByTitle("Edit Group") != null);

        clickOn("Cancel").clickOn("Groups").clickOn("Manage Groups").doubleClickOn("friends");
        assertTrue(findStageByTitle("Edit Group") != null);
    }
}
