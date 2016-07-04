package address.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import address.browser.BrowserManager;
import hubturbo.embeddedbrowser.BrowserType;
import javafx.collections.FXCollections;

import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testfx.api.FxToolkit;

/**
 * A JUnit {@link Rule} for running tests that requires
 * JavaFX initialisation.  To include in your test case, add the following code:
 * public JavafxRuntimeRule jfxRule = new JavafxRuntimeRule();
 */
public class JavafxRuntimeRule implements TestRule {

    public JavafxRuntimeRule() {
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

    @BeforeClass
    public void setUp() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.hideStage();
    }

    @AfterClass
    public void tearDown() throws Exception {
        FxToolkit.cleanupStages();
    }

    @Override
    public Statement apply(Statement statement, Description description) {

        return new OnJFXThreadStatement();
    }

    private static class OnJFXThreadStatement extends Statement {
        @Override
        public void evaluate() throws Throwable {

        }
    }
}