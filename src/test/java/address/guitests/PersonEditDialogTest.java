package address.guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.hasText;

public class PersonEditDialogTest extends GuiTestBase {


    @Test
    public void testUpdatePerson() {
        // TODO: find out why context menu does not appear in headless tests
        clickOn("Hans").push(KeyCode.E).targetWindow("Edit Person")
                .clickOn("#cityField").write("My City").clickOn("OK");

        clickOn("Hans").push(KeyCode.E).targetWindow("Edit Person");

        verifyThat("#cityField", hasText("My City"));
    }
}
