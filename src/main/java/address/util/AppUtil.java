package address.util;

import address.MainApp;
import javafx.scene.image.Image;

import java.net.URL;

/**
 * A container for App specific utility functions
 */
public class AppUtil {

    public static URL getResourceUrl(String resourcePath) {
        return MainApp.class.getResource(resourcePath);
    }

    public static Image getImage(String imagePath) {
        return new Image(MainApp.class.getResourceAsStream(imagePath));
    }

}
