package address.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * An utility class for URL
 */
public class UrlUtil {

    public static boolean compareBaseUrls(URL url1, URL url2) {

        if (url1 == null || url2 == null) {
            return false;
        }
        boolean isSameBaseUrl = url1.getHost().toLowerCase().replaceFirst("www.", "")
                                .equals(url2.getHost().replaceFirst("www.", "").toLowerCase())
                                && url1.getPath().replaceAll("/", "").toLowerCase()
                                   .equals(url2.getPath().replaceAll("/", "").toLowerCase());
        return isSameBaseUrl;
    }

    /**
     * Gets a list of URL that may be potentially be used in the future.
     * The {@link URL} that will be used at the current moment must be in the param urls. And it will be to determine
     * which URL wil be used in the future.
     * @param urls The list of URL that contains URL that may be potentially used in the future.
     * @param selectedUrl The index of the current URL in the list(urls) that will be used at this moment.
     * @param noOfFuturisticUrl The number of future URLs you wish to obtain from the list.
     * @return A list of url that may be potentially used in the future. Range of list size: [0, noOfFuturisticUrl]
     */
    public static List<URL> getFuturisticUrls(List<URL> urls, int selectedUrl, int noOfFuturisticUrl) {
        URL currentUrl = urls.get(selectedUrl);
        List<URL> listOfFuturisticUrl = new ArrayList<>();

        int count = 0;
        int i = 1;
        while (count < noOfFuturisticUrl && i < urls.size()) {
            URL url = urls.get((selectedUrl + i) % urls.size());
            if (url.equals(currentUrl) || listOfFuturisticUrl.contains(url)){
                i++;
                continue;
            }
            listOfFuturisticUrl.add(url);
            i++;
            count++;
        }
        return listOfFuturisticUrl;
    }

}
