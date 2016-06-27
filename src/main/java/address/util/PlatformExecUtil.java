package address.util;

import javafx.application.Platform;

import java.util.concurrent.*;

/**
 * Contains utility methods for running code in various ways,
 * on or off (but close to) the JavaFX application thread.
 */
public final class PlatformExecUtil {

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    public static void runLater(Runnable action) {
        Platform.runLater(action);
    }

    /**
     * If called from FX thread, will run immediately and return completed future.
     * If called outside FX thread, returns immediately, callback is queued and run asynchronously on FX thread.
     */
    public static <R> Future<R> call(Callable<R> callback) {
        final FutureTask<R> task = new FutureTask<>(callback);
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            runLater(task);
        }
        return task;
    }

    public static void runLaterDelayed(Runnable action, long delay, TimeUnit unit) {
        SCHEDULER.schedule(() -> Platform.runLater(action), delay, unit);
    }

    public static <R> Future<R> callLaterDelayed(Callable<R> action, long delay, TimeUnit unit) {
        final FutureTask<R> task = new FutureTask<R>(action);
        SCHEDULER.schedule(() -> Platform.runLater(task), delay, unit);
        return task;
    }

    /**
     * Blocks until the JavaFX event queue becomes empty.
     */
    public static void waitOnFxThread() {
        runLaterAndWait(() -> {});
    }

    /**
     * Runs an action on the JavaFX Application Thread and blocks until it completes.
     * Similar to {@link #runAndWait(Runnable) runAndWait}, but always enqueues the
     * action, eschewing checking the current thread.
     *
     * @param action The action to run on the JavaFX Application Thread
     */
    public static void runLaterAndWait(Runnable action) {
        assert action != null : "Non-null action required";
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            action.run();
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Synchronous version of Platform.runLater, like SwingUtilities.invokeAndWait.
     * Caveat: will execute immediately when invoked from the JavaFX application thread
     * instead of being queued up for execution.
     *
     * @param action The action to execute on the JavaFX Application Thread.
     */
    public static void runAndWait(Runnable action) {
        assert action != null : "Non-null action required";
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        runLaterAndWait(action);
    }

    private PlatformExecUtil() {
    }
}
