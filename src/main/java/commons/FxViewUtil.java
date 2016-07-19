package commons;

import com.sun.javafx.scene.control.skin.VirtualScrollBar;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;

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
                .map(obj -> (VirtualScrollBar) obj).findAny();
    }

    /**
     * Configure the image view to be circular in shape.
     * @param imageView
     */
    public static void configureCircularImageView(ImageView imageView) {
        double xyPositionAndRadius = imageView.getFitHeight() / 2.0;
        imageView.setClip(new Circle(xyPositionAndRadius, xyPositionAndRadius, xyPositionAndRadius));
    }

}
