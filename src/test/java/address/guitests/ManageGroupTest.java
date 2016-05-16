package address.guitests;

import org.junit.Test;

public class ManageGroupTest extends GuiTestBase {
    @Test
    public void manageGroupsFromMenuTest() {
        clickOn("Groups").clickOn("Manage Groups");

        findStageByTitle("List of Contact Groups");
    }
}
