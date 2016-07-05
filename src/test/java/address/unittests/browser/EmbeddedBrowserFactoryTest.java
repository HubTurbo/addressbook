package address.unittests.browser;

import address.browser.BrowserManager;
import address.util.JavafxRuntimeUtil;
import hubturbo.EmbeddedBrowser;
import hubturbo.embeddedbrowser.BrowserType;
import hubturbo.embeddedbrowser.EmbeddedBrowserFactory;
import hubturbo.embeddedbrowser.fxbrowser.FxBrowserAdapter;
import hubturbo.embeddedbrowser.jxbrowser.JxBrowserAdapter;
import javafx.collections.FXCollections;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * To test the EmbeddedBrowserFactory
 */
public class EmbeddedBrowserFactoryTest {


    @BeforeClass
    public static void setup() throws TimeoutException {
        JavafxRuntimeUtil.initRuntime();
        new BrowserManager(FXCollections.observableArrayList(), 1, BrowserType.FULL_FEATURE_BROWSER).initBrowser();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        JavafxRuntimeUtil.tearDownRuntime();
    }

    @Test
    public void testCreateBrowser_fullFeatureBrowser_success() {
        EmbeddedBrowser browser = EmbeddedBrowserFactory.createBrowser(BrowserType.FULL_FEATURE_BROWSER);
        assertNotNull(browser);
        assertTrue(browser instanceof JxBrowserAdapter);
    }

    @Test
    public void testCreateBrowser_limitedFeatureBrowser_success() {
        EmbeddedBrowser browser = EmbeddedBrowserFactory.createBrowser(BrowserType.LIMITED_FEATURE_BROWSER);
        assertNotNull(browser);
        assertTrue(browser instanceof FxBrowserAdapter);
    }

    @Test
    public void testCreateBrowser_invalidChoice_fail() {
        EmbeddedBrowser browser = null;
        try {
            browser = EmbeddedBrowserFactory.createBrowser(BrowserType.valueOf("SUPER_FEATURE_BROWSER"));
        } catch (IllegalArgumentException e) {
        }
        assertNull(browser);
    }

}
