package commons;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the UrlUtil methods.
 */
public class UrlUtilTest {
    
    private List<URL> listOfUrl;
    private final String GITHUB_ROOT_URL = "https://github.com/";

    public UrlUtilTest() throws MalformedURLException {
        listOfUrl = new ArrayList<>();
        listOfUrl.add(new URL(GITHUB_ROOT_URL + 1));
        listOfUrl.add(new URL(GITHUB_ROOT_URL + 2));
        listOfUrl.add(new URL(GITHUB_ROOT_URL + 3));
        listOfUrl.add(new URL(GITHUB_ROOT_URL + 4));
        listOfUrl.add(new URL(GITHUB_ROOT_URL + 5));
        listOfUrl.add(new URL(GITHUB_ROOT_URL));
        listOfUrl.add(new URL(GITHUB_ROOT_URL));
        listOfUrl.add(new URL(GITHUB_ROOT_URL));
        listOfUrl.add(new URL(GITHUB_ROOT_URL));
        listOfUrl.add(new URL(GITHUB_ROOT_URL + "a"));
        listOfUrl.add(new URL(GITHUB_ROOT_URL));
        listOfUrl.add(new URL(GITHUB_ROOT_URL));
        listOfUrl.add(new URL(GITHUB_ROOT_URL));
    }

    @Test
    public void testCompareBaseUrls_differentCapital_success() throws MalformedURLException {
        URL url1 = new URL("https://www.Google.com/a");
        URL url2 = new URL("https://www.google.com/A");
        assertTrue(UrlUtil.compareBaseUrls(url1, url2));
    }

    @Test
    public void testCompareBaseUrls_testWithAndWithoutWww_success() throws MalformedURLException {
        URL url1 = new URL("https://google.com/a");
        URL url2 = new URL("https://www.google.com/a");
        assertTrue(UrlUtil.compareBaseUrls(url1, url2));
    }

    @Test
    public void testCompareBaseUrls_differentSlashes_success() throws MalformedURLException {
        URL url1 = new URL("https://www.Google.com/a/acb/");
        URL url2 = new URL("https://www.google.com/A/acb");
        assertTrue(UrlUtil.compareBaseUrls(url1, url2));
    }

    @Test
    public void testCompareBaseUrls_differentUrl_fail() throws MalformedURLException {
        URL url1 = new URL("https://www.Google.com/a/ac_b/");
        URL url2 = new URL("https://www.google.com/A/acb");
        assertFalse(UrlUtil.compareBaseUrls(url1, url2));
    }
}
