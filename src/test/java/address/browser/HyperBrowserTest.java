package address.browser;

import address.util.JavafxRuntimeUtil;
import address.util.PlatformExecUtil;
import hubturbo.embeddedbrowser.BrowserType;
import hubturbo.embeddedbrowser.HyperBrowser;
import hubturbo.embeddedbrowser.page.Page;
import address.util.UrlUtil;
import javafx.collections.FXCollections;
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

import static junit.framework.TestCase.fail;
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
        JavafxRuntimeUtil.initRuntime();
        new BrowserManager(FXCollections.emptyObservableList(),
                1, BrowserType.FULL_FEATURE_BROWSER).initBrowser();
    }

    @AfterClass
    public static void teardown() throws Exception {
        JavafxRuntimeUtil.tearDownRuntime();
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
        PlatformExecUtil.runLaterAndWait(() -> {
            HyperBrowser browser = new HyperBrowser(BrowserType.LIMITED_FEATURE_BROWSER, 1, Optional.empty());
            URL url = null;
            try {
                url = new URL("https://github.com");
            } catch (MalformedURLException e) {
                fail();
            }
            Page page = browser.loadUrl(url).get(0);
            try {
                assertTrue(UrlUtil.compareBaseUrls(page.getBrowser().getOriginUrl(), url));
            } catch (MalformedURLException e) {
                fail();
            }
        });
    }

    @Test
    public void testLimitedFeatureBrowser_loadUrls_urlsAssigned() throws MalformedURLException, IllegalAccessException, NoSuchFieldException, InterruptedException {
        PlatformExecUtil.runLaterAndWait(() -> {
            HyperBrowser browser = new HyperBrowser(BrowserType.LIMITED_FEATURE_BROWSER, 3, Optional.empty());
            Page page = browser.loadUrls(listOfUrl.get(0), listOfUrl.subList(1,3)).get(0);
            try {
                assertTrue(UrlUtil.compareBaseUrls(page.getBrowser().getOriginUrl(), listOfUrl.get(0)));
            } catch (MalformedURLException e) {
                fail();
            }

            Field pages = null;
            try {
                pages = browser.getClass().getDeclaredField("pages");
            } catch (NoSuchFieldException e) {
                fail();
            }
            pages.setAccessible(true);
            List<Page> listOfPages = null;
            try {
                listOfPages = (List<Page>) pages.get(browser);
            } catch (IllegalAccessException e) {
                fail();
            }
            listOfPages.remove(page);
            try {
                Page secondPage = listOfPages.remove(0);
                assertTrue(UrlUtil.compareBaseUrls(secondPage.getBrowser().getOriginUrl(), listOfUrl.get(1)));
                Page thirdPage = listOfPages.remove(0);
                assertTrue(UrlUtil.compareBaseUrls(thirdPage.getBrowser().getOriginUrl(), listOfUrl.get(2)));
            } catch (MalformedURLException e) {
                fail();
            }
        });
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
