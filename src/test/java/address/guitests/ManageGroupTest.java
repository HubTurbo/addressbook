package address.guitests;

import address.GuiTestBase;
import org.junit.Test;

public class ManageGroupTest extends GuiTestBase {
    @Test
    public void manageGroupsFromMenuTest() {
        clickOn("Groups").clickOn("Manage Groups");

        findStageByTitle("List of Contact Groups");
    }
}
