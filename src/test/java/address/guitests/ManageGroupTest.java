package address.guitests;

import org.junit.Test;

public class ManageGroupTest extends GuiTestBase {
    @Test
    public void manageGroupsFromMenuTest() {
        clickOn("Groups").clickOn("Manage Groups");

        targetWindow("List of Contact Groups");
    }
}
