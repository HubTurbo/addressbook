package address.util;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

/**
 * A utility class to contain methods related to the Screen.
 */
public final class ScreenUtil {

    public static ScreenSize getRecommendedScreenSize() {
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        return new ScreenSize(primScreenBounds.getWidth() * 0.5, primScreenBounds.getHeight() * 0.7);
    }
}
