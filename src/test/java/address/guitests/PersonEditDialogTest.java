package address.guitests;

import javafx.scene.Node;
import javafx.scene.effect.ImageInput;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.ImagePattern;
import org.junit.Test;
import org.loadui.testfx.GuiTest;

import javax.imageio.ImageIO;

import java.io.File;

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
