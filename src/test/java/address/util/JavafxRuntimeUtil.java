package address.util;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.rules.Timeout;
import org.testfx.api.FxToolkit;

import java.util.concurrent.TimeoutException;

/**
 *
 */
public class JavafxRuntimeUtil {

    private static Boolean isInitialized = false;

    public static void initRuntime(){
        if (!isInitialized) {
            try {
                FxToolkit.registerPrimaryStage();
                FxToolkit.hideStage();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            isInitialized = true;
        }
    }

    @AfterClass
    public static void tearDownRuntime() throws Exception {
        FxToolkit.cleanupStages();
    }

}
