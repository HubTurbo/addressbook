package address.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * An utility class for URL
 */
public class UrlUtil {

    public static boolean compareBaseUrls(URL url1, URL url2) {
        if (url1 == null || url2 == null) {
            return false;
        }
        return url1.getHost().equals(url2.getHost()) && url1.getPath().equals(url2.getPath());
    }

}
