package address.guitests;

import address.UITest;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ManageGroupTest extends UITest {
    @Test
    public void manageGroupsFromMenuTest() {
        clickOn("Groups").clickOn("Manage Groups");

        assertTrue(findStageByTitle("List of Contact Groups") != null);
    }
}
