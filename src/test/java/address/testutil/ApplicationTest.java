package address.testutil;

import guitests.GuiRobot;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Preloader;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.testfx.api.FxToolkit;
import org.testfx.api.annotation.Unstable;
import org.testfx.framework.junit.ApplicationAdapter;
import org.testfx.framework.junit.ApplicationFixture;

/**
 * Code mostly from TestFX
 */
public abstract class ApplicationTest extends GuiRobot implements ApplicationFixture {

    //---------------------------------------------------------------------------------------------
    // STATIC METHODS.
    //---------------------------------------------------------------------------------------------

    @Unstable(reason = "is missing apidocs")
    public static void launch(Class<? extends Application> appClass,
                              String... appArgs) throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupApplication(appClass, appArgs);
    }

    //---------------------------------------------------------------------------------------------
    // METHODS.
    //---------------------------------------------------------------------------------------------

    @Before
    @Unstable(reason = "is missing apidocs")
    public final void internalBefore()
            throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.setupApplication(() -> new ApplicationAdapter(this));
        sleep(1000);
    }

    @After
    @Unstable(reason = "is missing apidocs")
    public final void internalAfter()
            throws Exception {
        FxToolkit.cleanupApplication(new ApplicationAdapter(this));
    }

    @Override
    @Unstable(reason = "is missing apidocs")
    public void init()
            throws Exception {}

    @Override
    @Unstable(reason = "is missing apidocs")
    public abstract void start(Stage stage)
            throws Exception;

    @Override
    @Unstable(reason = "is missing apidocs")
    public void stop()
            throws Exception {}

    @Deprecated
    public final HostServices getHostServices() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public final Application.Parameters getParameters() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public final void notifyPreloader(Preloader.PreloaderNotification notification) {
        throw new UnsupportedOperationException();
    }
}
