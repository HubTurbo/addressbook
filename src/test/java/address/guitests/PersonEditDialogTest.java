package address.guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.hasText;

public class PersonEditDialogTest extends GuiTestBase {

    @Test
    public void testUpdatePerson() {
        // TODO: find out why context menu does not appear in headless tests
        // TODO: This does not work if the person is not visible (i.e. too far down the list)
        String nameOfPersonToEdit = "Cornelia";
        clickOn(nameOfPersonToEdit).push(KeyCode.E).targetWindow("Edit Person")
                .clickOn("#cityField").write("My City").clickOn("OK");
        clickOn(nameOfPersonToEdit).push(KeyCode.E).targetWindow("Edit Person");

        verifyThat("#cityField", hasText("My City"));
    }
}
