package address.util;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * A utility class to contain methods related to the Screen.
 */
public final class ScreenUtil {

    public static ImmutablePair<Double, Double> getRecommendedScreenSize() {
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        return new ImmutablePair<>(primScreenBounds.getWidth() * 0.5, primScreenBounds.getHeight() * 0.7);
    }
}
