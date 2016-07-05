package address.util;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.testfx.api.FxToolkit;

import java.util.concurrent.TimeoutException;

/**
 *
 */
public class JavafxRuntimeUtil {

    @BeforeClass
    public static void initRuntime(){
        try {
            FxToolkit.registerPrimaryStage();
            FxToolkit.hideStage();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDownRuntime() throws Exception {
        FxToolkit.cleanupStages();
    }

}
