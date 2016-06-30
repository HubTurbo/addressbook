package address.guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.hasText;

public class TagEditDialogTest extends GuiTestBase {
    @Test
    public void editTagDialogTest() {
        clickOn("Tags").clickOn("Manage Tags").doubleClickOn("friends");
        verifyThat("#tagNameField", hasText("friends"));

        clickOn("#tagNameField").push(KeyCode.SHORTCUT, KeyCode.A).eraseText(1).write("family");
        verifyThat("#tagNameField", hasText("family"));

        push(KeyCode.ENTER).doubleClickOn(targetWindow("List of Tags").lookup("family").tryQuery().get());
        verifyThat("#tagNameField", hasText("family"));
    }

    @Test
    public void tagEditDialogLoadTest() {
        clickOn("Tags").clickOn("New Tag");

        verifyThat("#tagNameField", hasText(""));

        clickOn("Cancel").clickOn("Tags").clickOn("Manage Tags")
                .doubleClickOn(targetWindow("List of Tags").lookup("friends").tryQuery().get());

        targetWindow("Edit Tag");
        verifyThat("#tagNameField", hasText("friends"));
    }

    //TODO: verify that tag list has been updated, currently no way to refresh tag list
    @Test
    public void updateTagTest() {
        clickOn("Tags").clickOn("Manage Tags")
                .doubleClickOn(targetWindow("List of Tags").lookup("friends").tryQuery().get())
                .clickOn("#tagNameField").eraseText(7).write("changed tag").clickOn("OK");
    }
}
