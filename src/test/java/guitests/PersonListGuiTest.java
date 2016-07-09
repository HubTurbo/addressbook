package guitests;

import javafx.scene.control.Label;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PersonListGuiTest extends GuiTestBase {
    @Test
    public void dragAndDrop_firstToSecond() {
        Label hansNameLabel = getNameLabelOf("Hans");
        Label ruthIdLabel = getNameLabelOf("Ruth");
        assertTrue(hansNameLabel.localToScreen(0, 0).getY() < ruthIdLabel.localToScreen(0, 0).getY());
        guiRobot.drag("Hans").dropTo("Heinz");// drag from first to start of 3rd (slightly further down between 2nd and 3rd)

        Label hansNameLabel2 = getNameLabelOf("Hans");
        Label ruthIdLabel2 = getNameLabelOf("Ruth");
        assertTrue(hansNameLabel2.localToScreen(0, 0).getY() > ruthIdLabel2.localToScreen(0, 0).getY());
    }

    private Label getNameLabelOf(String name) {
        return (Label) guiRobot.lookup(name).tryQuery().get();
    }
}
