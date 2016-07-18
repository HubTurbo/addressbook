package hubturbo.launcher;

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
 *
 * This assumes that the application main class can be found in the library files (in lib folder)
 */
public class Launcher extends Application {
    private static final String ERROR_LAUNCH = "Failed to launch";
    private static final String ERROR_RUNNING = "Failed to run application";
    private static final String ERROR_TRY_AGAIN = "Please try again, or contact developer if it keeps failing.";
    public static final String SPECIFICATION_FILE_PATH = "update/UpdateSpecification";

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
        if (hasUpdate()) {
            System.out.println("Update found");
            Process process = runUpdateMigrator();
            while (process.isAlive()) {}
            System.out.println("Running main application");
            runMainApplication();
            stop();
        }
        System.out.println("No updates found.");
        runMainApplication();
        stop();
    }

    private boolean hasUpdate() {
        File updateSpecificationFile = new File(SPECIFICATION_FILE_PATH);
        return updateSpecificationFile.exists();
    }

    private Process runUpdateMigrator() throws IOException {
        try {
            String classPath = File.pathSeparator + "lib" + File.separator + "*";
            String command = String.format("java -ea -cp %s hubturbo.updater.UpdateMigrator --update-specification=%s --source-dir=%s", classPath, SPECIFICATION_FILE_PATH, "update");
            System.out.println("Starting updater migrator: " + command);
            Process process = Runtime.getRuntime().exec(command, null, new File(System.getProperty("user.dir")));
            System.out.println("Update migrator launched");
            return process;
        } catch (IOException e) {
            throw new IOException(ERROR_RUNNING, e);
        }
    }

    private void runMainApplication() throws IOException {
        try {
            System.out.println("Starting main application");
            String classPath = File.pathSeparator + "lib" + File.separator + "*";
            String command = String.format("java -ea -cp %s address.MainApp", classPath);
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
