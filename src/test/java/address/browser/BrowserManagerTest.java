package address.browser;

import address.util.JavafxRuntimeRule;
import hubturbo.embeddedbrowser.BrowserType;
import javafx.collections.FXCollections;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.testfx.api.FxToolkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertNotNull;


/**
 * Tests the BrowserManager behaviours and functionality.
 */
public class BrowserManagerTest {

    /**
     * To run test cases on JavaFX thread.
     */

    @BeforeClass
    public static void setUp() throws Exception {
        new BrowserManager(FXCollections.emptyObservableList(),
                1, BrowserType.FULL_FEATURE_BROWSER).initBrowser();
        FxToolkit.registerPrimaryStage();
        FxToolkit.hideStage();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        FxToolkit.cleanupStages();
    }

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
