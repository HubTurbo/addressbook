package guitests;

import org.junit.Test;

public class TagListTest extends GuiTestBase {
    @Test
    public void manageTagsFromMenuTest() {
        clickOn("Tags").clickOn("Manage Tags");

        targetWindow("List of Tags");
    }
}
