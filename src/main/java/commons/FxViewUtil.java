package commons;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;

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

    /**
     * Configure the image view to be circular in shape.
     * @param imageView
     */
    public static void configureCircularImageView(ImageView imageView) {
        double xyPositionAndRadius = imageView.getFitHeight() / 2.0;
        imageView.setClip(new Circle(xyPositionAndRadius, xyPositionAndRadius, xyPositionAndRadius));
    }

}
