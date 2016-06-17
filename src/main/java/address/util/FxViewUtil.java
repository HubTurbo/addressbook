package address.util;

import com.sun.javafx.scene.control.skin.VirtualScrollBar;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;

import java.util.Optional;

/**
 * Contains utility methods for JavaFX views
 */
public class FxViewUtil {

    public static void applyAnchorBoundaryParameters(Node node, double left, double right, double top, double bottom) {
        AnchorPane.setBottomAnchor(node, bottom);
        AnchorPane.setLeftAnchor(node, left);
        AnchorPane.setRightAnchor(node, right);
        AnchorPane.setTopAnchor(node, top);
    }

    public static Optional<VirtualScrollBar> getScrollBarFromListView(ListView listView) {
        return listView.lookupAll(".scroll-bar")
                .stream()
                .filter(foundSb -> ((VirtualScrollBar) foundSb).getOrientation() == Orientation.VERTICAL)
                .map(obj -> (VirtualScrollBar)obj).findAny();
    }

}
