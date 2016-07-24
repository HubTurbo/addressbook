package address.util;

import address.MainApp;

import java.net.URL;

/**
 * A container for App specific utility functions
 */
public class AppUtil {

    public static URL getResourceUrl(String resourcePath) {
        return MainApp.class.getResource(resourcePath);
    }
}
