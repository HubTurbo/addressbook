package address.browser;

import address.testutils.JavafxThreadingRule;
import hubturbo.embeddedbrowser.BrowserType;
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


    @Test
    public void testNecessaryBrowserResources_resourcesNotNull() throws NoSuchMethodException,
                                                                        InvocationTargetException,
                                                                        IllegalAccessException {
        BrowserManager manager = new BrowserManager(FXCollections.observableArrayList(), 3,
                                                    BrowserType.FULL_FEATURE_BROWSER);
        manager.start();
        assertNotNull(manager.getHyperBrowserView());
        Method method = manager.getClass().getDeclaredMethod("getBrowserInitialScreen");
        method.setAccessible(true);
        method.invoke(manager);
    }


}
