package address.browser;

import address.util.JavafxRuntimeUtil;
import hubturbo.embeddedbrowser.BrowserType;
import javafx.collections.FXCollections;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertNotNull;


/**
 * Tests the BrowserManager behaviours and functionality.
 */
public class BrowserManagerTest {

    @BeforeClass
    public static void setup() {
        JavafxRuntimeUtil.initRuntime();
        new BrowserManager(FXCollections.emptyObservableList(), 1, BrowserType.FULL_FEATURE_BROWSER).initBrowser();
    }

    @AfterClass
    public static void teardown() throws Exception {
        JavafxRuntimeUtil.tearDownRuntime();
    }

    @Test
    public void testNecessaryBrowserResources_resourcesNotNull() throws NoSuchMethodException,
                                                                        InvocationTargetException,
                                                                        IllegalAccessException {
        BrowserManager manager = new BrowserManager(FXCollections.observableArrayList(), 1,
                                                    BrowserType.FULL_FEATURE_BROWSER);
        manager.start();
        assertNotNull(manager.getHyperBrowserView());
        Method method = manager.getClass().getDeclaredMethod("getBrowserInitialScreen");
        method.setAccessible(true);
        method.invoke(manager);
        manager.freeBrowserResources();
    }
}
