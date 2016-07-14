package guitests;

import javafx.scene.input.KeyCode;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.hasText;

public class TagEditDialogTest extends GuiTestBase {
    @Test
    public void editTagDialogTest() {
        guiRobot.clickOn("Tags").clickOn("Manage Tags").doubleClickOn("friends");
        verifyThat("#tagNameField", hasText("friends"));

        guiRobot.clickOn("#tagNameField").push(KeyCode.SHORTCUT, KeyCode.A).eraseText(1).write("family");
        guiRobot.sleep(1, TimeUnit.SECONDS);
        verifyThat("#tagNameField", hasText("family"));

        guiRobot.push(KeyCode.ENTER).doubleClickOn(guiRobot.targetWindow("List of Tags").lookup("family").tryQuery().get());
        verifyThat("#tagNameField", hasText("family"));
    }

    @Test
    public void tagEditDialogLoadTest() {
        guiRobot.clickOn("Tags").clickOn("New Tag");

        verifyThat("#tagNameField", hasText(""));

        guiRobot.clickOn("Cancel").clickOn("Tags").clickOn("Manage Tags")
                .doubleClickOn(guiRobot.targetWindow("List of Tags").lookup("friends").tryQuery().get());

        guiRobot.targetWindow("Edit Tag");
        verifyThat("#tagNameField", hasText("friends"));
    }

    //TODO: verify that tag list has been updated, currently no way to refresh tag list
    @Test
    public void updateTagTest() {
        guiRobot.clickOn("Tags").clickOn("Manage Tags")
                .doubleClickOn(guiRobot.targetWindow("List of Tags").lookup("friends").tryQuery().get())
                .clickOn("#tagNameField").eraseText(7).write("changed tag").clickOn("OK");
    }
}
