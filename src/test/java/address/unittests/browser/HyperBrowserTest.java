package address.unittests.browser;

import address.browser.HyperBrowser;
import address.browser.embeddedbrowser.EmbeddedBrowser;
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
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

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

    List<URL> listOfUrl = Arrays.asList(new URL("https://github.com"),
            new URL("https://google.com.sg"),
            new URL("https://sg.yahoo.com"),
            new URL("https://www.nus.edu.sg"),
            new URL("https://www.ntu.edu.sg"),
            new URL("https://bitbucket.org"));

    public HyperBrowserTest() throws MalformedURLException {
    }

    @Test
    public void testFullFeatureBrowser_loadUrl_urlAssigned() throws MalformedURLException, InterruptedException {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.FULL_FEATURE_BROWSER, 1, Optional.empty());
        Page page = browser.loadUrl(new URL("https://github.com"));
        waitForUrlToBeLoadedInBrowser(page.getBrowser(), 10);
        assertTrue(UrlUtil.compareBaseUrls(page.getBrowser().getUrl(), new URL("https://github.com")));
    }

    @Test
    public void testFullFeatureBrowser_loadUrls_urlsAssigned() throws MalformedURLException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.FULL_FEATURE_BROWSER, 3, Optional.empty());

        List<URL> listOfUrl = Arrays.asList(new URL("https://github.com"), new URL("https://google.com.sg"), new URL("https://sg.yahoo.com"));
        Page page = browser.loadUrl(listOfUrl.get(0), listOfUrl.subList(1,3));
        waitForUrlToBeLoadedInBrowser(page.getBrowser(), 10);
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
    public void testFullFeatureBrowser_loadUrlsMultipleTimesNoOverLapping_correctUrlsAssigned() throws MalformedURLException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.FULL_FEATURE_BROWSER, 3, Optional.empty());


        browser.loadUrl(listOfUrl.get(0), listOfUrl.subList(1,3));
        TimeUnit.SECONDS.sleep(3);
        Page page = browser.loadUrl(listOfUrl.get(3), listOfUrl.subList(4,6));
        waitForUrlToBeLoadedInBrowser(page.getBrowser(), 10);
        Field pages = browser.getClass().getDeclaredField("pages");
        pages.setAccessible(true);
        List<Page> listOfPages = (List<Page>) pages.get(browser);
        assertTrue(listOfPages.size() == 3);
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(0).getBrowser().getUrl(), listOfUrl.get(3)));
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(1).getBrowser().getUrl(), listOfUrl.get(4)));
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(2).getBrowser().getUrl(), listOfUrl.get(5)));
    }

    @Test
    public void testFullFeatureBrowser_loadUrlsMultipleTimesOverLapping_correctUrlsAssigned() throws MalformedURLException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.FULL_FEATURE_BROWSER, 3, Optional.empty());
        browser.loadUrl(listOfUrl.get(0), listOfUrl.subList(1,3));
        TimeUnit.SECONDS.sleep(3);
        Page page = browser.loadUrl(listOfUrl.get(2), listOfUrl.subList(3,5));
        waitForUrlToBeLoadedInBrowser(page.getBrowser(), 10);

        Field pages = browser.getClass().getDeclaredField("pages");
        pages.setAccessible(true);
        List<Page> listOfPages = (List<Page>) pages.get(browser);
        assertTrue(listOfPages.size() == 3);
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(0).getBrowser().getUrl(), listOfUrl.get(2)));
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(1).getBrowser().getUrl(), listOfUrl.get(3)));
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(2).getBrowser().getUrl(), listOfUrl.get(4)));
    }

    @Test
    public void testFullFeatureBrowser_loadUrlsMultipleTimesWithDuplicates_correctUrlsAssigned() throws MalformedURLException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.FULL_FEATURE_BROWSER, 3, Optional.empty());
        browser.loadUrl(listOfUrl.get(0), listOfUrl.subList(1,3));
        TimeUnit.SECONDS.sleep(3);
        Page page = browser.loadUrl(listOfUrl.get(2), Arrays.asList(listOfUrl.get(0), listOfUrl.get(0)));
        waitForUrlToBeLoadedInBrowser(page.getBrowser(), 10);

        Field pages = browser.getClass().getDeclaredField("pages");
        pages.setAccessible(true);
        List<Page> listOfPages = (List<Page>) pages.get(browser);
        assertTrue(listOfPages.size() == 2);
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(0).getBrowser().getUrl(), listOfUrl.get(0)));
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(1).getBrowser().getUrl(), listOfUrl.get(2)));
    }

    @Test
    public void testFullFeatureBrowser_loadUrlsThenLoadUrl_displayedUrlRemoved() throws MalformedURLException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.FULL_FEATURE_BROWSER, 3, Optional.empty());
        browser.loadUrl(listOfUrl.get(0), listOfUrl.subList(1,3));
        TimeUnit.SECONDS.sleep(3);
        Page page = browser.loadUrl(listOfUrl.get(4));
        waitForUrlToBeLoadedInBrowser(page.getBrowser(), 10);

        Field pages = browser.getClass().getDeclaredField("pages");
        pages.setAccessible(true);
        List<Page> listOfPages = (List<Page>) pages.get(browser);
        assertTrue(listOfPages.size() == 3);
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(2).getBrowser().getUrl(), listOfUrl.get(4)));
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(0).getBrowser().getUrl(), listOfUrl.get(1)));
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(1).getBrowser().getUrl(), listOfUrl.get(2)));
    }

    @Test
    public void testLimitedFeatureBrowser_loadUrl_urlAssigned() throws MalformedURLException, InterruptedException {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.LIMITED_FEATURE_BROWSER, 1, Optional.empty());
        URL url = new URL("https://github.com");
        Page page = browser.loadUrl(url);
        waitForUrlToBeLoadedInBrowser(page.getBrowser(), 10);
        assertTrue(UrlUtil.compareBaseUrls(page.getBrowser().getUrl(), url));
    }

    @Test
    public void testLimitedFeatureBrowser_loadUrls_urlsAssigned() throws MalformedURLException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.LIMITED_FEATURE_BROWSER, 3, Optional.empty());
        Page page = browser.loadUrl(listOfUrl.get(0), listOfUrl.subList(1,3));
        waitForUrlToBeLoadedInBrowser(page.getBrowser(), 10);
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
    public void testClearPage_removeOnePage_pageRemoved() throws MalformedURLException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.FULL_FEATURE_BROWSER, 3, Optional.empty());
        Page page = browser.loadUrl(listOfUrl.get(0), listOfUrl.subList(1,3));
        waitForUrlToBeLoadedInBrowser(page.getBrowser(), 10);
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
        Page page = browser.loadUrl(new URL("https://github.com"));
        assertTrue(UrlUtil.compareBaseUrls(browser.getDisplayedUrl(), new URL("https://github.com")));
    }

    public void waitForUrlToBeLoadedInBrowser(EmbeddedBrowser browser, int noOfTries) throws InterruptedException {
        int count = 0;
        while (count < noOfTries) {
            try {
                URL url = browser.getUrl();
                if (url != null) {
                    return;
                }
            } catch (MalformedURLException e) {
            }
            Thread.sleep(1);
        }
    }
}
