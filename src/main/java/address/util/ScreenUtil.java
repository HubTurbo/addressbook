package address.util;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

/**
 * A utility class to contain methods related to the Screen.
 */
public final class ScreenUtil {

    public static final int DEFAULT_HEIGHT = 600;
    public static final int DEFAULT_WIDTH = 740;

    public static ScreenSize getRecommendedScreenSize() {
        //Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        return new ScreenSize((double)DEFAULT_WIDTH, (double)DEFAULT_HEIGHT);
    }
}
