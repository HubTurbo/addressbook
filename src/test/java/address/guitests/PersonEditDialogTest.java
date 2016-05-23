package address.guitests;

import javafx.scene.Node;
import org.junit.Test;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.hasText;

public class PersonEditDialogTest extends GuiTestBase {

    private Node getPersonListContextMenuEdit() {
        return lookup("#personListContextMenu").lookup("Edit").tryQuery().get();
    }

    @Test
    public void testUpdatePerson() {
        rightClickOn("Hans").clickOn(getPersonListContextMenuEdit()).targetWindow("Edit Person")
                .clickOn("#cityField").write("My City").clickOn("OK");

        rightClickOn("Hans").clickOn(getPersonListContextMenuEdit()).targetWindow("Edit Person");

        verifyThat("#cityField", hasText("My City"));
    }
}
