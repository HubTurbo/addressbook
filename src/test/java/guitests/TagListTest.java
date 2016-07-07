package guitests;

import org.junit.Test;

public class TagListTest extends GuiTestBase {
    @Test
    public void manageTagsFromMenuTest() {
        guiRobot.clickOn("Tags").clickOn("Manage Tags");

        guiRobot.targetWindow("List of Tags");
    }
}
