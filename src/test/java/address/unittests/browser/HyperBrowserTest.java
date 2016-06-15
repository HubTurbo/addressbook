package address.unittests.browser;

import address.browser.HyperBrowser;
import address.browser.page.Page;
import address.util.JavafxThreadingRule;
import address.util.UrlUtil;
import org.junit.Test;
import org.junit.Rule;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test the behaviour of the HyperBrowser. To ensure correct linkage between HyperBrowser and its dependency :
 * EmbeddedBrowser, and by using two different types of browser engine, Java WebView and JxBrowser.
 * Does not test the functionality of the browser engine (i.e Java WebView or JxBrowser)
 */
public class HyperBrowserTest {

    @Rule
    /**
     * To run test cases on JavaFX thread.
     */
    public JavafxThreadingRule javafxRule = new JavafxThreadingRule();

    @Test
    public void testFullFeatureBrowser_loadUrl_urlAssigned() throws MalformedURLException {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.FULL_FEATURE_BROWSER, 1, Optional.empty());
        Page page = browser.loadUrls(new URL("https://github.com"));
        assertTrue(UrlUtil.compareBaseUrls(page.getBrowser().getUrl(), new URL("https://github.com")));
    }

    @Test
    public void testFullFeatureBrowser_loadUrls_urlsAssigned() throws MalformedURLException, IllegalAccessException, NoSuchFieldException {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.FULL_FEATURE_BROWSER, 3, Optional.empty());

        List<URL> listOfUrl = Arrays.asList(new URL("https://github.com"), new URL("https://google.com.sg"), new URL("https://sg.yahoo.com"));
        Page page = browser.loadUrls(listOfUrl.get(0), listOfUrl.subList(1,3));
        assertTrue(UrlUtil.compareBaseUrls(page.getBrowser().getUrl(), listOfUrl.get(0)));

        Field pages = browser.getClass().getDeclaredField("pages");
        pages.setAccessible(true);
        List<Page> listOfPages = (List<Page>) pages.get(browser);
        listOfPages.remove(page);
        Page secondPage = listOfPages.remove(0);
        assertTrue(UrlUtil.compareBaseUrls(secondPage.getBrowser().getUrl(), listOfUrl.get(1)));
        Page thirdPage = listOfPages.remove(0);
        assertTrue(UrlUtil.compareBaseUrls(thirdPage.getBrowser().getUrl(), listOfUrl.get(2)));
    }

    @Test
    public void testLimitedFeatureBrowser_loadUrl_urlAssigned() throws MalformedURLException, InterruptedException {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.LIMITED_FEATURE_BROWSER, 1, Optional.empty());
        URL url = new URL("https://github.com");
        Page page = browser.loadUrls(url);
        Thread.sleep(2);
        assertTrue(UrlUtil.compareBaseUrls(page.getBrowser().getUrl(), url));
    }

    @Test
    public void testLimitedFeatureBrowser_loadUrls_urlsAssigned() throws MalformedURLException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.LIMITED_FEATURE_BROWSER, 3, Optional.empty());

        List<URL> listOfUrl = Arrays.asList(new URL("https://github.com"), new URL("https://google.com.sg"), new URL("https://sg.yahoo.com"));
        Page page = browser.loadUrls(listOfUrl.get(0), listOfUrl.subList(1,3));
        Thread.sleep(2);
        assertTrue(UrlUtil.compareBaseUrls(page.getBrowser().getUrl(), listOfUrl.get(0)));

        Field pages = browser.getClass().getDeclaredField("pages");
        pages.setAccessible(true);
        List<Page> listOfPages = (List<Page>) pages.get(browser);
        listOfPages.remove(page);
        Page secondPage = listOfPages.remove(0);
        assertTrue(UrlUtil.compareBaseUrls(secondPage.getBrowser().getUrl(), listOfUrl.get(1)));
        Page thirdPage = listOfPages.remove(0);
        assertTrue(UrlUtil.compareBaseUrls(thirdPage.getBrowser().getUrl(), listOfUrl.get(2)));
    }

    @Test
    public void testClearPage_removeOnePage_pageRemoved() throws MalformedURLException, IllegalAccessException, NoSuchFieldException {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.FULL_FEATURE_BROWSER, 3, Optional.empty());

        List<URL> listOfUrl = Arrays.asList(new URL("https://github.com"), new URL("https://google.com.sg"), new URL("https://sg.yahoo.com"));
        Page page = browser.loadUrls(listOfUrl.get(0), listOfUrl.subList(1,3));
        browser.clearPage(page.getBrowser().getUrl());
        Field pages = browser.getClass().getDeclaredField("pages");
        pages.setAccessible(true);
        List<Page> listOfPages = (List<Page>) pages.get(browser);
        assertTrue(listOfPages.size() == 2);
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(0).getBrowser().getUrl(), listOfUrl.get(1)));
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(1).getBrowser().getUrl(), listOfUrl.get(2)));
    }

    @Test
    public void testGetDisplayedUrl_loadOnePage_getDisplayedUrlReturnPageUrl() throws MalformedURLException {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.FULL_FEATURE_BROWSER, 1, Optional.empty());
        Page page = browser.loadUrls(new URL("https://github.com"));
        assertTrue(UrlUtil.compareBaseUrls(browser.getDisplayedUrl(), new URL("https://github.com")));
    }

}
