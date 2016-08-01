package address.browser;

import address.testutil.TestUtil;
import commons.PlatformExecUtil;
import hubturbo.embeddedbrowser.BrowserType;
import javafx.collections.FXCollections;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertNotNull;


/**
 * Tests the BrowserManager behaviours and functionality.
 */
public class BrowserManagerTest {

    @BeforeClass
    public static void setup() throws TimeoutException {
        TestUtil.initRuntime();
    }

    @AfterClass
    public static void teardown() throws Exception {
        TestUtil.tearDownRuntime();
    }

    @Test
    public void testNecessaryBrowserResources_resourcesNotNull() throws NoSuchMethodException,
                                                                        InvocationTargetException,
                                                                        IllegalAccessException {
        BrowserManager manager = new BrowserManager(FXCollections.observableArrayList(), 3,
                                                    BrowserType.LIMITED_FEATURE_BROWSER);
        PlatformExecUtil.runLaterAndWait(() -> manager.start());
        assertNotNull(manager.getHyperBrowserView());
        Method method = manager.getClass().getDeclaredMethod("getBrowserInitialScreen");
        method.setAccessible(true);
        method.invoke(manager);
    }
}
