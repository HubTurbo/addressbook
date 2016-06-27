package address.util;

import address.MainApp;
import address.image.ImageManager;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import com.sun.javafx.scene.control.skin.VirtualScrollBar;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;
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

    public static Image getDragView(List<ReadOnlyViewablePerson> draggedPersons) {
        HBox container = new HBox(5);
        draggedPersons.stream().forEach(p -> {
            Optional<String> profilePicUrl = p.githubProfilePicUrl();
            ImageView imageView;
            if (profilePicUrl.isPresent()) {
                imageView = new ImageView(ImageManager.getInstance().getImage(profilePicUrl.get()));
            } else {
                imageView = new ImageView(getDefaultProfileImage());
            }
            imageView.setFitHeight(50.0);
            imageView.setFitWidth(50.0);
            configureCircularImageView(imageView);
            container.getChildren().add(imageView);
        });
        SnapshotParameters para = new SnapshotParameters();
        para.setFill(Color.TRANSPARENT);
        return container.snapshot(para, null);
    }

    public static Image getDefaultProfileImage() {
        return new Image(MainApp.class.getResourceAsStream("/images/default_profile_picture.png"));
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
