package hubturbo.updater;

import address.util.Version;
import address.util.VersionData;
import commons.FileUtil;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is meant to perform pending updates that have been successfully downloaded
 * It does so by reading local update specifications from a file, then replace specified files
 */
public class UpdateMigrator extends Application {
    private static final int MAX_RETRIES = 10;
    private static final int WAIT_TIME = 2000;
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
                stop();
            } catch (IOException e) {
                showErrorOnUpdatingDialog(e);
            }
        });
    }

    @Override
    public void stop() {
        Platform.exit();
        System.exit(0);
    }

    private void showWaitingWindow(Stage stage) {
        VBox windowMainLayout = new VBox(getUpdatingLabel());

        Scene scene = new Scene(windowMainLayout);

        stage.setTitle("Updater");
        stage.setScene(scene);
        stage.show();
    }

    private Label getUpdatingLabel() {
        Label updatingLabel = new Label();
        updatingLabel.setText("Applying updates to application...");
        updatingLabel.setPadding(new Insets(50));
        return updatingLabel;
    }

    private void run() throws IllegalArgumentException, IOException {
        if (!LocalUpdateSpecificationHelper.hasLocalUpdateSpecFile()) return;

        String updateSpecificationFilePath = LocalUpdateSpecificationHelper.getLocalUpdateSpecFilepath();
        String sourceDir = Updater.UPDATE_DIR;

        System.out.println("Getting update specifications from: " + updateSpecificationFilePath);
        List<String> updateSpecifications = getUpdateSpecifications(updateSpecificationFilePath);

        updateBackups();
        applyUpdateToAllFiles(sourceDir, updateSpecifications);
        deleteFile(updateSpecificationFilePath);
    }

    /**
     * Creates a new backup for the current version, and cleans up backups
     * @throws IOException
     */
    private void updateBackups() throws IOException {
        BackupHandler backupHandler = new BackupHandler(readCurrentVersionFromFile());
        backupHandler.createAppBackup();
        backupHandler.cleanupBackups();
    }

    private void deleteFile(String updateSpecFilePath) throws IOException {
        FileUtil.deleteFile(updateSpecFilePath);
    }

    private List<String> getUpdateSpecifications(String updateSpecificationFilePath) throws IOException {
        return LocalUpdateSpecificationHelper.readLocalUpdateSpecFile(updateSpecificationFilePath);
    }

    private Version readCurrentVersionFromFile() throws IOException {
        VersionData versionData = FileUtil.deserializeObjectFromJsonFile(new File("VersionData.json"), VersionData.class);
        return Version.fromString(versionData.getVersion());
    }

    /**
     * Attempts to update files with files from sourceDir
     *
     * @param sourceDir
     * @param filesToBeUpdated list of files' filepaths
     * @throws IOException
     */
    private void applyUpdateToAllFiles(String sourceDir, List<String> filesToBeUpdated) throws IOException {
        for (String fileToUpdate : filesToBeUpdated) {
            System.out.println("Updating file: " + fileToUpdate);
            updateFile(sourceDir, fileToUpdate);
        }
    }

    /**
     * Attempts to replace the file with a newer version
     *
     * In some platforms (Windows in particular), JAR file cannot be modified if it was executed and
     * the process it created has not ended yet. As such, we will make several tries with wait.
     */
    private void updateFile(String sourceDir, String fileToUpdate) throws IOException {
        Path source = Paths.get(sourceDir, fileToUpdate);
        Path dest = Paths.get(fileToUpdate);
        createFileAndParentDirs(dest);

        for (int i = 0; i < MAX_RETRIES; i++) {
            if (applyUpdate(source, dest)) return;
            if (i != MAX_RETRIES - 1) sleepFor(WAIT_TIME);
        }
        throw new IOException("Jar file cannot be updated. Most likely it is in use by another process.");

    }

    private void createFileAndParentDirs(Path dest) throws IOException {
        if (!FileUtil.isFileExists(dest.toString())) {
            FileUtil.createParentDirsOfFile(dest.toFile());
        }
    }

    /**
     * Attempts to sleep for a specified period
     *
     * @param sleepDurationInMilliseconds
     */
    private void sleepFor(int sleepDurationInMilliseconds) {
        try {
            Thread.sleep(sleepDurationInMilliseconds);
        } catch (InterruptedException e) {
            System.out.println("Error sleeping thread for: " + sleepDurationInMilliseconds);
        }
    }

    /**
     * Attempts to move the file from source to dest
     * Source file will not be kept and destination file will be overwritten
     *
     * @param source
     * @param dest
     * @return true if successful
     */
    private boolean applyUpdate(Path source, Path dest) {
        try {
            FileUtil.moveFile(source, dest, true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void showErrorOnUpdatingDialog(Exception e) {
        showErrorDialog("Failed to perform update", ERROR_ON_UPDATING_MESSAGE, e.getMessage());
    }

    private void showErrorDialog(String title, String header, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(message);
            alert.showAndWait();
            stop();
        });
    }
}
