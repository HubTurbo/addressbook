package address.util;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.testfx.api.FxToolkit;

import java.util.concurrent.TimeoutException;

/**
 *
 */
public class JavafxRuntimeUtil {

    public static void initRuntime() throws TimeoutException {
        FxToolkit.registerPrimaryStage();
        FxToolkit.hideStage();
    }

    @AfterClass
    public static void tearDownRuntime() throws Exception {
        FxToolkit.cleanupStages();
    }

}
