package address.unittests.browser;

import address.browser.BrowserManager;
import javafx.collections.FXCollections;
import org.junit.Test;


import static org.junit.Assert.assertNotNull;

/**
 * Tests the BrowserManager behaviours and functionality.
 */
public class BrowserManagerTest {

    @Test
    public void testNecessaryBrowserResources_resourcesFound() {
        BrowserManager manager = new BrowserManager(FXCollections.observableArrayList());
        assertNotNull(manager.getHyperBrowserView());
    }

}
