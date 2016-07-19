package guitests;

import address.model.datatypes.AddressBook;
import com.google.common.io.Files;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.loadui.testfx.GuiTest;

import java.io.File;
import java.io.IOException;

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
        robot.push(KeyCode.E);
        robot.clickOn("#firstNameField").push(KeyCode.SHORTCUT, KeyCode.A).eraseText(1).write("wahaha");
        file = GuiTest.captureScreenshot();
        Files.copy(file, new File("13.png"));
        fail();
    }


}
