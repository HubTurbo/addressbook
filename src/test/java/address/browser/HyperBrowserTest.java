package address.browser;

import address.util.PlatformExecUtil;
import address.testutil.TestUtil;
import hubturbo.embeddedbrowser.BrowserType;
import hubturbo.embeddedbrowser.HyperBrowser;
import hubturbo.embeddedbrowser.page.Page;
import address.util.UrlUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertTrue;

/**
 * Test the behaviour of the HyperBrowser. To ensure correct linkage between HyperBrowser and its dependency :
 * EmbeddedBrowser, and by using two different types of browser engine, Java WebView and JxBrowser.
 * Does not test the functionality of the browser engine (i.e Java WebView or JxBrowser)
 */
public class HyperBrowserTest {

    List<URL> listOfUrl = Arrays.asList(new URL("https://github.com"),
            new URL("https://google.com.sg"),
            new URL("https://sg.yahoo.com"),
            new URL("https://www.nus.edu.sg"),
            new URL("https://www.ntu.edu.sg"),
            new URL("https://bitbucket.org"));

    public HyperBrowserTest() throws MalformedURLException {
    }

    @BeforeClass
    public static void setup() throws TimeoutException {
        TestUtil.initRuntime();
        TestUtil.initBrowserInStatic();
    }

    @AfterClass
    public static void teardown() throws Exception {
        TestUtil.tearDownRuntime();
    }

    @Test
    public void testFullFeatureBrowser_loadUrl_urlAssigned() throws MalformedURLException, InterruptedException {
        HyperBrowser browser = new HyperBrowser(BrowserType.FULL_FEATURE_BROWSER, 1, Optional.empty());
        Page page = browser.loadUrl(new URL("https://github.com")).get(0);
        assertTrue(UrlUtil.compareBaseUrls(page.getBrowser().getOriginUrl(), new URL("https://github.com")));
    }

    @Test
    public void testFullFeatureBrowser_loadUrls_urlsAssigned() throws MalformedURLException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        HyperBrowser browser = new HyperBrowser(BrowserType.FULL_FEATURE_BROWSER, 3, Optional.empty());

        List<URL> listOfUrl = Arrays.asList(new URL("https://github.com"), new URL("https://google.com.sg"), new URL("https://sg.yahoo.com"));
        Page page = browser.loadUrls(listOfUrl.get(0), listOfUrl.subList(1,3)).get(0);
        assertTrue(UrlUtil.compareBaseUrls(page.getBrowser().getOriginUrl(), listOfUrl.get(0)));

        Field pages = browser.getClass().getDeclaredField("pages");
        pages.setAccessible(true);
        List<Page> listOfPages = (List<Page>) pages.get(browser);
        listOfPages.remove(page);
        Page secondPage = listOfPages.remove(0);
        assertTrue(UrlUtil.compareBaseUrls(secondPage.getBrowser().getOriginUrl(), listOfUrl.get(1)));
        Page thirdPage = listOfPages.remove(0);
        assertTrue(UrlUtil.compareBaseUrls(thirdPage.getBrowser().getOriginUrl(), listOfUrl.get(2)));
    }

    @Test
    public void testFullFeatureBrowser_loadUrlsMultipleTimesNoOverLapping_correctUrlsAssigned() throws MalformedURLException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        HyperBrowser browser = new HyperBrowser(BrowserType.FULL_FEATURE_BROWSER, 3, Optional.empty());


        browser.loadUrls(listOfUrl.get(0), listOfUrl.subList(1,3));
        TimeUnit.SECONDS.sleep(3);
        Page page = browser.loadUrls(listOfUrl.get(3), listOfUrl.subList(4,6)).get(0);
        Field pages = browser.getClass().getDeclaredField("pages");
        pages.setAccessible(true);
        List<Page> listOfPages = (List<Page>) pages.get(browser);
        assertTrue(listOfPages.size() == 3);
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(0).getBrowser().getOriginUrl(), listOfUrl.get(3)));
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(1).getBrowser().getOriginUrl(), listOfUrl.get(4)));
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(2).getBrowser().getOriginUrl(), listOfUrl.get(5)));
    }

    @Test
    public void testFullFeatureBrowser_loadUrlsMultipleTimesOverLapping_correctUrlsAssigned() throws MalformedURLException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        HyperBrowser browser = new HyperBrowser(BrowserType.FULL_FEATURE_BROWSER, 3, Optional.empty());
        browser.loadUrls(listOfUrl.get(0), listOfUrl.subList(1,3));
        TimeUnit.SECONDS.sleep(3);
        Page page = browser.loadUrls(listOfUrl.get(2), listOfUrl.subList(3,5)).get(0);

        Field pages = browser.getClass().getDeclaredField("pages");
        pages.setAccessible(true);
        List<Page> listOfPages = (List<Page>) pages.get(browser);
        assertTrue(listOfPages.size() == 3);
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(0).getBrowser().getOriginUrl(), listOfUrl.get(2)));
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(1).getBrowser().getOriginUrl(), listOfUrl.get(3)));
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(2).getBrowser().getOriginUrl(), listOfUrl.get(4)));
    }

    @Test
    public void testFullFeatureBrowser_loadUrlsMultipleTimesWithDuplicates_correctUrlsAssigned() throws MalformedURLException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        HyperBrowser browser = new HyperBrowser(BrowserType.FULL_FEATURE_BROWSER, 3, Optional.empty());
        browser.loadUrls(listOfUrl.get(0), listOfUrl.subList(1,3));
        TimeUnit.SECONDS.sleep(3);
        Page page = browser.loadUrls(listOfUrl.get(2), Arrays.asList(listOfUrl.get(0), listOfUrl.get(0))).get(0);

        Field pages = browser.getClass().getDeclaredField("pages");
        pages.setAccessible(true);
        List<Page> listOfPages = (List<Page>) pages.get(browser);
        assertTrue(listOfPages.size() == 2);
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(0).getBrowser().getOriginUrl(), listOfUrl.get(0)));
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(1).getBrowser().getOriginUrl(), listOfUrl.get(2)));
    }

    @Test
    public void testFullFeatureBrowser_loadUrlsThenLoadUrl_displayedUrlRemoved() throws MalformedURLException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        HyperBrowser browser = new HyperBrowser(BrowserType.FULL_FEATURE_BROWSER, 3, Optional.empty());
        browser.loadUrls(listOfUrl.get(0), listOfUrl.subList(1,3));
        TimeUnit.SECONDS.sleep(3);
        Page page = browser.loadUrl(listOfUrl.get(4)).get(0);

        Field pages = browser.getClass().getDeclaredField("pages");
        pages.setAccessible(true);
        List<Page> listOfPages = (List<Page>) pages.get(browser);
        assertTrue(listOfPages.size() == 3);
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(2).getBrowser().getOriginUrl(), listOfUrl.get(4)));
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(0).getBrowser().getOriginUrl(), listOfUrl.get(1)));
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(1).getBrowser().getOriginUrl(), listOfUrl.get(2)));
    }

    @Test
    public void testLimitedFeatureBrowser_loadUrl_urlAssigned() throws MalformedURLException, InterruptedException {
        final AtomicReference<HyperBrowser> browser = new AtomicReference<>();
        PlatformExecUtil.runLaterAndWait(() -> browser.set(new HyperBrowser(BrowserType.LIMITED_FEATURE_BROWSER, 1, Optional.empty())));
        URL url = new URL("https://github.com");
        final AtomicReference<Page> page = new AtomicReference<>();
        PlatformExecUtil.runLaterAndWait(() -> page.set(browser.get().loadUrl(url).get(0)));
        assertTrue(UrlUtil.compareBaseUrls(page.get().getBrowser().getOriginUrl(), url));
        browser.get().dispose();
    }

    @Test
    public void testLimitedFeatureBrowser_loadUrls_urlsAssigned() throws MalformedURLException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        final AtomicReference<HyperBrowser> browser = new AtomicReference<>();
        PlatformExecUtil.runLaterAndWait(() -> browser.set(new HyperBrowser(BrowserType.LIMITED_FEATURE_BROWSER, 3, Optional.empty())));
        final AtomicReference<Page> page = new AtomicReference<>();
        PlatformExecUtil.runLaterAndWait(() -> page.set(browser.get().loadUrls(listOfUrl.get(0), listOfUrl.subList(1,3)).get(0)));
        assertTrue(UrlUtil.compareBaseUrls(page.get().getBrowser().getOriginUrl(), listOfUrl.get(0)));
        Field pages = browser.get().getClass().getDeclaredField("pages");
        pages.setAccessible(true);
        List<Page> listOfPages = (List<Page>) pages.get(browser.get());
        listOfPages.remove(page.get());
        Page secondPage = listOfPages.remove(0);
        assertTrue(UrlUtil.compareBaseUrls(secondPage.getBrowser().getOriginUrl(), listOfUrl.get(1)));
        Page thirdPage = listOfPages.remove(0);
        assertTrue(UrlUtil.compareBaseUrls(thirdPage.getBrowser().getOriginUrl(), listOfUrl.get(2)));
        browser.get().dispose();
    }

    @Test
    public void testClearPage_removeOnePage_pageRemoved() throws MalformedURLException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        HyperBrowser browser = new HyperBrowser(BrowserType.FULL_FEATURE_BROWSER, 3, Optional.empty());
        Page page = browser.loadUrls(listOfUrl.get(0), listOfUrl.subList(1,3)).get(0);
        browser.clearPage(page.getBrowser().getOriginUrl());
        Field pages = browser.getClass().getDeclaredField("pages");
        pages.setAccessible(true);
        List<Page> listOfPages = (List<Page>) pages.get(browser);
        assertTrue(listOfPages.size() == 2);
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(0).getBrowser().getOriginUrl(), listOfUrl.get(1)));
        assertTrue(UrlUtil.compareBaseUrls(listOfPages.get(1).getBrowser().getOriginUrl(), listOfUrl.get(2)));
    }

    @Test
    public void testGetDisplayedUrl_loadOnePage_getDisplayedUrlReturnPageUrl() throws MalformedURLException {
        HyperBrowser browser = new HyperBrowser(BrowserType.FULL_FEATURE_BROWSER, 1, Optional.empty());
        Page page = browser.loadUrl(new URL("https://github.com")).get(0);
        assertTrue(UrlUtil.compareBaseUrls(browser.getDisplayedUrl(), new URL("https://github.com")));
    }
}
