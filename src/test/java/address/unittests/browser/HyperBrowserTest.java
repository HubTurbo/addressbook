package address.unittests.browser;

import address.browser.HyperBrowser;
import address.browser.page.Page;
import address.util.JavafxThreadingRule;
import org.junit.Test;
import org.junit.Rule;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class HyperBrowserTest {

    @Rule
    /**
     * To run test cases on JavaFX thread.
     */
    public JavafxThreadingRule javafxRule = new JavafxThreadingRule();

    @Test
    public void testFullFeatureBrowser_loadUrl_loadSuccess() {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.FULL_FEATURE_BROWSER, 1, Optional.empty());
        Page page = null;
        try {
            page = browser.loadUrls(new URL("https://github.com"));
        } catch (MalformedURLException e) {
            fail();
        }
        while (page.isPageLoading()) ;
        System.out.println("page url = " + page.getBrowser().getUrlString().toLowerCase());
        assertTrue(page.getBrowser().getUrlString().toLowerCase().equals("https://github.com/"));
    }

    @Test
    public void testLimitedFeatureBrowser_loadUrl_loadSuccess() throws InterruptedException, MalformedURLException {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.LIMITED_FEATURE_BROWSER, 1, Optional.empty());
        Page page = browser.loadUrls(new URL("https://github.com"));
        while (page.isPageLoading());
        assertTrue(page.getBrowser().getUrlString().toLowerCase().equals("https://github.com/"));
    }

    @Test
    public void testFullFeatureBrowser_loadUrls_loadSuccess() throws MalformedURLException {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.FULL_FEATURE_BROWSER, 3, Optional.empty());

        List<URL> url = Arrays.asList(new URL("https://google.com"), new URL("https://yahoo.com"));

        Page page = browser.loadUrls(new URL("https://github.com"), url);
        while (page.isPageLoading());
        assertTrue(page.getBrowser().getUrlString().toLowerCase().equals("https://github.com/"));
    }


}
