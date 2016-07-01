package address.guitests;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PersonOverviewTest extends GuiTestBase {
    @Test
    public void dragAndDrop_firstToSecond() {
        Label hansIdLabel = getIdLabelOf("Hans");
        Label ruthIdLabel = getIdLabelOf("Ruth");
        assertTrue(hansIdLabel.localToScreen(0, 0).getY() < ruthIdLabel.localToScreen(0, 0).getY());
        drag("Hans").dropTo("Heinz");// drag from first to start of 3rd (slightly further down between 2nd and 3rd)

        Label hansIdLabel2 = getIdLabelOf("Hans");
        Label ruthIdLabel2 = getIdLabelOf("Ruth");
        assertTrue(hansIdLabel2.localToScreen(0, 0).getY() > ruthIdLabel2.localToScreen(0, 0).getY());
    }

    private Label getIdLabelOf(String name) {
        Parent nameCell = lookup(name).tryQuery().get().getParent();
        Label personIdLabel = (Label) nameCell.getChildrenUnmodifiable().stream()
                .filter(child -> {
                    if (!(child instanceof Label)) return false;
                    return ((Label) child).getText().equals("#TBD");
                })
                .findFirst().get();
        return personIdLabel;
    }
}
