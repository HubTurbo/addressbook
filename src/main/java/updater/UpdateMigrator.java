package updater;

import commons.LocalUpdateSpecificationHelper;
import commons.UpdaterUtil;
import commons.Version;
import commons.VersionData;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is meant to perform pending updates that have been successfully downloaded
 * It does so by reading local update specifications from a file, and then replacing specified files
 */
public class UpdateMigrator extends Application {
    private static final String CUR_DIR = ".";
    private static final String LAUNCHER_FILE_REGEX = "addressbook-V\\d\\.\\d\\.\\d(ea)?\\.jar";
    private static final String ERROR_ON_UPDATING_MESSAGE = "There was an error in updating.";
    private static final String UPDATE_DIR = "update";

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

        stage.setTitle("UpdateManager");
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

        System.out.println("Getting update specifications from: " + updateSpecificationFilePath);
        List<String> updateSpecifications = getUpdateSpecifications(updateSpecificationFilePath);

        updateBackups();
        deleteLaunchers();
        applyUpdateToAllFiles(UPDATE_DIR, updateSpecifications);
        UpdaterUtil.deleteFile(updateSpecificationFilePath);
    }

    /**
     * Creates a new backup for the current version, and cleans up backups
     * @throws IOException
     */
    private void updateBackups() throws IOException {
        System.out.println("Updating backups");
        BackupHandler backupHandler = new BackupHandler(readCurrentVersionFromFile());
        backupHandler.createAppBackup();
        backupHandler.cleanupBackups();
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
            UpdaterUtil.updateFile(sourceDir, fileToUpdate);
        }
    }

    private void showErrorOnUpdatingDialog(IOException e) {
        showErrorDialog("Failed to perform update", ERROR_ON_UPDATING_MESSAGE, e.toString());
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


    /**
     * Attempts to upgrade the launcher file
     *
     * Assumes that the new version has the same path from UPDATE_DIR
     *
     * @throws IOException
     */
    public void deleteLaunchers() throws IOException {
        System.out.println("Deleting launchers");
        FileUtil.deleteFile(findComponentFileName(CUR_DIR, LAUNCHER_FILE_REGEX));
    }

    private String findComponentFileName(String dirPath, String regex) throws FileNotFoundException {
        File curDir = new File(dirPath);
        String[] curDirFilesNames = curDir.list();
        if (curDirFilesNames == null) assert false : "Not given a directory to check for component!";
        return dirPath + File.separator + getFileNameOfRegexMatch(curDirFilesNames, regex);
    }

    /**
     * Attempts to get the file which matching file name as the given regex
     *
     * @param curDirFilesNames
     * @param regex
     * @return file name of matching file
     * @throws FileNotFoundException if no file name matches the given regex
     */
    private String getFileNameOfRegexMatch(String[] curDirFilesNames, String regex) throws FileNotFoundException {
        for (String fileName : curDirFilesNames) {
            if (fileName.matches(regex)) return fileName;
        }
        throw new FileNotFoundException("File not found!");
    }
}
