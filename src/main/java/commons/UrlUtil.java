package commons;

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
        return url1.getHost().toLowerCase().replaceFirst("www.", "")
                .equals(url2.getHost().replaceFirst("www.", "").toLowerCase())
                && url1.getPath().replaceAll("/", "").toLowerCase()
                .equals(url2.getPath().replaceAll("/", "").toLowerCase());
    }

    /**
     * Gets a list of URL that may be potentially be used in the future.
     * The {@link URL} that will be used at the current moment must be in the param urls. And it will be to determine
     * which URL may be used in the future.
     * @param urls The list of URL that contains URL that may be potentially used in the future.
     * @param selectedUrl The index of the current URL in the list(urls) that will be used at this moment.
     * @param noOfFutureUrl The number of future URLs you wish to obtain from the list.
     * @return A list of url that may be potentially used in the future. Range of list size: [0, noOfFutureUrl]
     */
    public static List<URL> getFutureUrls(List<URL> urls, int selectedUrl, int noOfFutureUrl) {
        URL currentUrl = urls.get(selectedUrl);
        List<URL> listOfFutureUrl = new ArrayList<>();

        int count = 0;
        int i = 1;
        while (count < noOfFutureUrl && i < urls.size()) {
            URL url = urls.get((selectedUrl + i) % urls.size());
            if (url.equals(currentUrl) || listOfFutureUrl.contains(url)){
                i++;
                continue;
            }
            listOfFutureUrl.add(url);
            i++;
            count++;
        }
        return listOfFutureUrl;
    }

}
