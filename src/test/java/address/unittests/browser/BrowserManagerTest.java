package address.unittests.browser;

import address.browser.BrowserManager;
import address.util.JavafxThreadingRule;
import hubturbo.embeddedbrowser.BrowserConfig;
import hubturbo.embeddedbrowser.HyperBrowser;
import javafx.collections.FXCollections;
import org.junit.Rule;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the BrowserManager behaviours and functionality.
 */
public class BrowserManagerTest {

    @Rule
    /**
     * To run test cases on JavaFX thread.
     */
    public JavafxThreadingRule javafxRule = new JavafxThreadingRule();

    private BrowserConfig config;

    public BrowserManagerTest() {
        config = new BrowserConfig(HyperBrowser.RECOMMENDED_NUMBER_OF_PAGES);
    }

    @Test
    public void testNecessaryBrowserResources_resourcesNotNull() throws NoSuchMethodException,
                                                                        InvocationTargetException,
                                                                        IllegalAccessException {
        BrowserManager manager = new BrowserManager(FXCollections.observableArrayList(), config);
        manager.start();
        assertNotNull(manager.getHyperBrowserView());
        Method method = manager.getClass().getDeclaredMethod("getBrowserInitialScreen");
        method.setAccessible(true);
        method.invoke(manager);
    }


}
