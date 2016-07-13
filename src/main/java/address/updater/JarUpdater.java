package address.updater;

import address.util.AppLogger;
import address.util.FileUtil;
import address.util.LoggerManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is meant to read update specifications from a file, then replace specified files
 *
 * Note: This class will be compiled into a JAR on its own
 * If you made any changes to this class, run gradle task compileJarUpdater
 *
 * Options:
 * --update-specification the update specification file on which files to be updated
 * --source-dir the main directory which files to be updated
 */
public class JarUpdater extends Application {
    private static final AppLogger logger = LoggerManager.getLogger(JarUpdater.class);

    private static final int MAX_RETRIES = 10;
    private static final int WAIT_TIME = 2000;
    private static final String UPDATE_SPECIFICATION_KEY = "update-specification";
    private static final String SOURCE_DIR_KEY = "source-dir";
    private static final String ERROR_ON_RUNNING_APP_MESSAGE = "Application not called properly, " +
                                                               "please contact developer.";
    private static final String ERROR_ON_UPDATING_MESSAGE = "There was an error in updating.";

    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    public static void main(String[] args) { // NOPMD
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        showWaitingWindow(stage);
        pool.execute(() -> {
            try {
                run();
            } catch (IllegalArgumentException e) {
                showInvalidProgramArgumentErrorDialog();
            } catch (IOException e) {
                showErrorOnUpdatingDialog();
            }
        });
    }

    private void showWaitingWindow(Stage stage) {
        stage.setTitle("Updater");
        VBox windowMainLayout = new VBox();
        Scene scene = new Scene(windowMainLayout);
        stage.setScene(scene);

        Label updatingLabel = getUpdatingLabel();

        windowMainLayout.getChildren().addAll(updatingLabel);

        stage.show();
    }

    private Label getUpdatingLabel() {
        Label updatingLabel = new Label();
        updatingLabel.setText("Applying update to application...");
        updatingLabel.setPadding(new Insets(50));
        return updatingLabel;
    }

    private void run() throws IllegalArgumentException, IOException {
        HashMap<String, String> commandLineArgs = new HashMap<>(getParameters().getNamed());

        String updateSpecificationFilepath = commandLineArgs.get(UPDATE_SPECIFICATION_KEY);
        String sourceDir = commandLineArgs.get(SOURCE_DIR_KEY);

        if (updateSpecificationFilepath == null || sourceDir == null) {
            logger.info("Error: File path or source dir is null.");
            throw new IllegalArgumentException("Please specify the filepath to update specification " +
                                               "and the source directory of the update files.");
        } else {
            logger.info("{}: {}", UPDATE_SPECIFICATION_KEY, updateSpecificationFilepath);
            logger.info("{}: {}", SOURCE_DIR_KEY, sourceDir);
        }

        List<String> localUpdateData;

        try {
            localUpdateData = LocalUpdateSpecificationHelper.readLocalUpdateSpecFile(updateSpecificationFilepath);
        } catch (IOException e) {
            logger.info("Failed to read local update data");
            throw e;
        }
        logger.info("{} updates to be applied", localUpdateData.size());
        applyUpdateToAllFiles(sourceDir, localUpdateData);
        logger.info("Update successful");

        stop();
    }

    @Override
    public void stop() {
        Platform.exit();
        System.exit(0);
    }

    private void applyUpdateToAllFiles(String sourceDir, List<String> filesToBeUpdated) throws IOException {
        for (String fileToUpdate : filesToBeUpdated) {
            applyUpdate(Paths.get(sourceDir, fileToUpdate), Paths.get(fileToUpdate));
        }
    }

    /**
     * Applies update to a file.
     *
     * Technically replaces a file with a newer version with a guard to wait until the file is modifiable.
     *
     * In some platforms (Windows in particular), JAR file cannot be modified if it was executed and
     * the process it created has not ended yet. As such, we will make several tries with wait.
     */
    private void applyUpdate(Path source, Path dest) throws IOException {
        logger.info("Applying update for {}", dest);

        if (!FileUtil.isFileExists(dest.toString())) {
            FileUtil.createParentDirsOfFile(dest.toFile());
        }

        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                FileUtil.moveFile(source, dest, true);
                return;
            } catch (IOException e) {
                logger.info("Failed to move file {} to {}. Might be due to original JAR still in use.",
                        source.getFileName(), dest.getFileName());
            }

            try {
                logger.info("Waiting for a while before trying again.");
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                logger.warn("Failed to wait for a while");
            }
        }

        throw new IOException("Jar file cannot be updated. Most likely it is in use by another process.");
    }

    private void showInvalidProgramArgumentErrorDialog() {
        showErrorDialog("Failed to run updater", ERROR_ON_RUNNING_APP_MESSAGE);
    }

    private void showErrorOnUpdatingDialog() {
        showErrorDialog("Failed to update", ERROR_ON_UPDATING_MESSAGE);
    }

    private void showErrorDialog(String header, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(header);
            alert.setContentText(message);
            alert.showAndWait();
            stop();
        });
    }
}
