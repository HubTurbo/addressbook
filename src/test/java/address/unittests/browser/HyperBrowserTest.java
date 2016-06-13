package address.unittests.browser;

import address.browser.HyperBrowser;
import address.browser.page.Page;
import address.util.JavafxThreadingRule;
import org.junit.Test;
import org.junit.Rule;

import java.net.MalformedURLException;
import java.net.URL;
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
    public void testFullFeatureBrowser_LoadSuccess() {
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
    public void testLimitedFeatureBrowser_LoadSuccess() {
        HyperBrowser browser = new HyperBrowser(HyperBrowser.LIMITED_FEATURE_BROWSER, 1, Optional.empty());
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


}
