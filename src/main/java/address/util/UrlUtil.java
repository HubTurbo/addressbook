package address.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * An utility class for URL
 */
public class UrlUtil {

    public static String getBaseUrl(String url) {
        URL parsedUrl = null;
        String parsedString = null;
        try {
            parsedUrl = new URL(url);
            parsedString = parsedUrl.toURI().normalize().toASCIIString();
        } catch (MalformedURLException e) {
            return "";
        } catch (URISyntaxException e) {
            return "";
        }
        return parsedString;
    }

    public static boolean compareBaseUrls(URL url1, URL url2) {
        try {
            return url1.toURI().normalize().equals(url2.toURI().normalize())
        } catch (URISyntaxException e) {
            return false;
        }
    }

}
