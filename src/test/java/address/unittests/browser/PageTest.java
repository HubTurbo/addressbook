package address.unittests.browser;

import address.util.JavafxThreadingRule;
import hubturbo.EmbeddedBrowser;
import hubturbo.embeddedbrowser.BrowserType;
import hubturbo.embeddedbrowser.EmbeddedBrowserFactory;
import org.junit.Rule;
import org.junit.Test;

/**
 * To test the Page methods.
 */
public class PageTest {

    @Rule
    private JavafxThreadingRule rule = new JavafxThreadingRule();

    @Test
    public void testElementByClass_fullFeatureBrowser_elementFound() {
        EmbeddedBrowser browser = EmbeddedBrowserFactory.createBrowser(BrowserType.FULL_FEATURE_BROWSER);
        
    }

}
