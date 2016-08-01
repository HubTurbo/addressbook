package address.browser;

import commons.PlatformExecUtil;
import address.testutil.TestUtil;
import hubturbo.embeddedbrowser.BrowserType;
import hubturbo.embeddedbrowser.HyperBrowser;
import hubturbo.embeddedbrowser.page.Page;
import commons.UrlUtil;
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
    }

    @AfterClass
    public static void teardown() throws Exception {
        TestUtil.tearDownRuntime();
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
}
