package launcher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Launcher for the address book application
 * Will also launch the update migrator before the application to apply any pending updates
 */
public class Launcher extends Application {
    private static final String CLASS_PATH_REGEX = File.pathSeparator + "lib" + File.separator + "*";
    private static final String ERROR_LAUNCH = "Failed to launch";
    private static final String ERROR_RUNNING = "Failed to run application";
    private static final String ERROR_TRY_AGAIN = "Please try again, or contact developer if it keeps failing.";

    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    @Override
    public void start(Stage primaryStage) throws Exception {
        pool.execute(() -> {
            try {
                run();
            } catch (IOException e) {
                showErrorDialogAndQuit(ERROR_LAUNCH, e.getMessage(), ERROR_TRY_AGAIN);
            }
        });
    }

    private void run() throws IOException {
        runUpdateMigratorAndWait();
        runMainApplication();
        stop();
    }

    private void runUpdateMigratorAndWait() throws IOException {
        try {
            String command = String.format("java -ea -cp %s UpdateMigrator", CLASS_PATH_REGEX);
            System.out.println("Starting updater migrator: " + command);
            Process process = Runtime.getRuntime().exec(command, null, new File(System.getProperty("user.dir")));
            System.out.println("Update migrator launched");
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new IOException(ERROR_RUNNING, e);
        }
    }

    private void runMainApplication() throws IOException {
        try {
            String command = String.format("java -ea -cp %s address.MainApp", CLASS_PATH_REGEX);
            System.out.println("Starting main application: " + command);
            Runtime.getRuntime().exec(command, null, new File(System.getProperty("user.dir")));
            System.out.println("Main application launched");
        } catch (IOException e) {
            throw new IOException(ERROR_RUNNING, e);
        }
    }

    private void showErrorDialogAndQuit(String title, String headerText, String contentText) {
        Platform.runLater(() -> {
            final Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(headerText);
            alert.setContentText(contentText);
            alert.showAndWait();
            stop();
        });
    }

    @Override
    public void stop() {
        Platform.exit();
        System.exit(0);
    }
}
