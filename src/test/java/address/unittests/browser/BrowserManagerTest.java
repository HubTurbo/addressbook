package address.unittests.browser;

import address.browser.BrowserManager;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the BrowserManager behaviours and functionality.
 */
public class BrowserManagerTest {

    @Test
    public void testNecessaryBrowserResources_resourcesFound() {
        BrowserManager manager = new BrowserManager(Collections.emptyList());
        assertNotNull(manager.getHyperBrowserView());
    }

}
