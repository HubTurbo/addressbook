package address.browser;

import address.util.JavafxRuntimeRule;
import hubturbo.embeddedbrowser.BrowserType;
import javafx.collections.FXCollections;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertNotNull;


/**
 * Tests the BrowserManager behaviours and functionality.
 */
public class BrowserManagerTest {

    @Rule
    /**
     * To run test cases on JavaFX thread.
     */
    public JavafxRuntimeRule javafxRule = new JavafxRuntimeRule();

    @BeforeClass
    public void setup(){
        ExecutorService executor = Executors.newFixedThreadPool(1);

        Runnable task = () -> new BrowserManager(FXCollections.emptyObservableList(),
                1, BrowserType.FULL_FEATURE_BROWSER).initBrowser();
        Future<?> future = executor.submit(task);
        try {
            future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
