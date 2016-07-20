package guitests;

import address.model.datatypes.AddressBook;
import com.google.common.io.Files;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Window;
import org.junit.Test;
import org.loadui.testfx.GuiTest;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;

/**
 * Created by YL Lim on 19/7/2016.
 */
public class SimpleKeyBoardTest extends GuiTestBase {

    @Override
    protected AddressBook getInitialData() {
        return td.book;
    }

    @Test
    public void testKeyBoard() throws IOException {
        File file = null;
        GuiRobot robot = new GuiRobot();
        robot.clickOn("#filterField");
        assertTrue(robot.lookup("#filterField").tryQuery().get().isFocused());
        robot.write("qwertyuiop[]asdfghjklzxcvbnm1234567890");
        file = GuiTest.captureScreenshot();
        Files.copy(file, new File("11.png"));

        robot.clickOn("#filterField");
        assertTrue(robot.lookup("#filterField").tryQuery().get().isFocused());
        robot.push(KeyCode.SHORTCUT, KeyCode.A).eraseText(1);
        file = GuiTest.captureScreenshot();
        Files.copy(file, new File("12.png"));

        robot.clickOn("Alice");
        robot.push(KeyCode.E).sleep(500);

        List<Window> windows = robot.listTargetWindows();

        //robot.targetWindow(windows.get(1));
        robot.interact(() -> windows.get(2).requestFocus());

        TextField node = robot.lookup("#firstNameField").query();
       // doubleClickOnNode(robot, node);
        robot.doubleClickOn("#firstNameField").write("wahaha");

        //node = robot.lookup("#lastNameField").query();
        //doubleClickOnNode(robot, node);
        robot.doubleClickOn("#lastNameField").write("hehehe");

        file = GuiTest.captureScreenshot();
        Files.copy(file, new File("13.png"));
        fail();
    }

    public void doubleClickOnNode(GuiRobot robot, TextField node) {
        Bounds bounds = node.localToScreen(node.getBoundsInLocal());
        double x = bounds.getMinX() + bounds.getWidth() /2;
        double y = bounds.getMinY() + bounds.getHeight() / 2;
        robot.doubleClickOn(x, y);
    }


}
