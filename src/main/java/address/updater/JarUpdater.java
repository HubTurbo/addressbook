package address.updater;

import address.util.FileUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Group;
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
 * Note: This class will be compiled into a JAR on its own
 *
 * If you made any changes to this class, run gradle task compileJarUpdater
 *
 * Options:
 * --update-specification
 */
public class JarUpdater extends Application {
    private static final int MAX_RETRY = 10;
    private static final int WAIT_TIME = 2000;
    private static final String ERROR_ON_UPDATING_MESSAGE = "There was an error in updating.";
    
    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    public static void main(String[] args) { // NOPMD
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        initLogger();
        showWaitingWindow(stage);
        pool.execute(() -> {
            try {
                run();
            } catch (IllegalArgumentException e) {
                log(e.getMessage());
            } catch (IOException e) {
                log(e.getMessage());
                showErrorOnUpdatingDialog();
            }

            quit();
        });
    }

    private void showWaitingWindow(Stage stage) {
        stage.setTitle("Applying Updates");
        VBox windowMainLayout = new VBox();
        Group root = new Group();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        scene.setRoot(windowMainLayout);

        Label updatingLabel = new Label();
        updatingLabel.setText("Applying update to application...");
        updatingLabel.setPadding(new Insets(50));

        windowMainLayout.getChildren().addAll(updatingLabel);

        stage.show();
    }

    private void run() throws IllegalArgumentException, IOException {
        HashMap<String, String> commandLineArgs = new HashMap<>(getParameters().getNamed());

        String updateSpecificationFilepath = commandLineArgs.get("update-specification");
        String sourceDir = commandLineArgs.get("source-dir");

        if (updateSpecificationFilepath == null || sourceDir == null) {
            throw new IllegalArgumentException("Please specify the filepath to update specification " +
                                               "and the source directory of the update files.");
        } else {
            log("update-specification: " + updateSpecificationFilepath);
            log("source-dir: " + sourceDir);
        }

        List<String> localUpdateData;

        try {
            localUpdateData = LocalUpdateSpecificationHelper.readLocalUpdateSpecFile(updateSpecificationFilepath);
        } catch (IOException e) {
            log("Failed to read local update data");
            throw e;
        }

        if (localUpdateData.isEmpty()) {
            log("No update to be applied");
            return;
        }

        applyUpdateToAllFiles(sourceDir, localUpdateData);

        quit();
    }

    public void quit() {
        Platform.exit();
        System.exit(0);
    }

    private void applyUpdateToAllFiles(String sourceDir, List<String> filesToBeUpdated) throws IOException {
        for (String affectedFile : filesToBeUpdated) {
            applyUpdate(Paths.get(sourceDir + File.separator + affectedFile), Paths.get(affectedFile));
        }
    }

    /**
     * In some platforms (Windows in particular), JAR file cannot be modified if it was executed and
     * the process has not ended yet. As such, we will make several tries with wait.
     */
    private void applyUpdate(Path source, Path dest) throws IOException {
        log("Applying update for " + dest.toString());

        for (int i = 0; i < MAX_RETRY; i++) {
            try {
                FileUtil.moveFile(source, dest, true);
                return;
            } catch (IOException e) {
                log(String.format("Failed to move file %s to %s. Might be due to original JAR still in use.",
                        source.getFileName(), dest.getFileName()));
            }

            try {
                log("Wait for a while before trying again.");
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                log("Failed to wait for a while");
            }
        }

        throw new IOException("Jar file cannot be updated. Most likely is in use by another process.");
    }

    private void showErrorOnUpdatingDialog() {
        String header = "Failed to update";
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(header);
            alert.setContentText(ERROR_ON_UPDATING_MESSAGE);
            alert.showAndWait();
            quit();
        });
    }

    private void initLogger() {
        try {
            PrintStream out = new PrintStream(new FileOutputStream(new File("update.log")),
                    true, "UTF-8");
            System.setOut(out);
        } catch (FileNotFoundException e) {
            log("File not found, will not create logger");
        } catch (UnsupportedEncodingException e) {
            log("Encoding not supported, will not create logger");
        }
    }

    public static void log(String message) {
        System.out.println(message);
    }
}
