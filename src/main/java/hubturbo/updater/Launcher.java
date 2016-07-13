package hubturbo.updater;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Launcher for the address book application
 *
 * Launcher JAR will contain all of addressbook required JARs (libraries and dependencies) and
 * the main application JAR.
 */
public class Launcher extends Application {
    private static final String ERROR_LAUNCH = "Failed to launch";
    private static final String ERROR_INSTALL = "Failed to install";
    private static final String ERROR_RUNNING = "Failed to run application";
    private static final String ERROR_TRY_AGAIN = "Please try again, or contact developer if it keeps failing.";

    private final ExecutorService pool = Executors.newSingleThreadExecutor();
    private ProgressBar progressBar;
    private Label loadingLabel;

    @Override
    public void start(Stage primaryStage) throws Exception {
        showWaitingWindow(primaryStage);
        pool.execute(() -> {
            try {
                run();
            } catch (IOException e) {
                showErrorDialogAndQuit(ERROR_LAUNCH, e.getMessage(), ERROR_TRY_AGAIN);
            }
        });
    }

    private void run() throws IOException {
        Installer installer = new Installer();
        Platform.runLater(() -> loadingLabel.setText("Installing..."));
        try {
            installer.runInstall(loadingLabel, progressBar);
        } catch (IOException e) {
            throw new IOException(ERROR_INSTALL, e);
        }

        Platform.runLater(() -> loadingLabel.setText("Launching AddressBook..."));
        try {
            startMainApplication();
        } catch (IOException e) {
            throw new IOException(ERROR_RUNNING, e);
        }

        stop();
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

    private void startMainApplication() throws IOException {
        System.out.println("Starting main application");

        String classPath = File.pathSeparator + "lib" + File.separator + "*";
        String command = String.format("java -ea -cp %s address.MainApp", classPath);

        Runtime.getRuntime().exec(command, null, new File(System.getProperty("user.dir")));

        System.out.println("Main application launched");
    }

    private void showWaitingWindow(Stage stage) {
        stage.setTitle("Launcher");
        VBox windowMainLayout = new VBox();
        Scene scene = new Scene(windowMainLayout);
        stage.setScene(scene);

        loadingLabel = getLoadingLabel();
        progressBar = getProgressBar();

        final VBox vb = new VBox();
        vb.setSpacing(30);
        vb.setPadding(new Insets(40));
        vb.setAlignment(Pos.CENTER);
        vb.getChildren().addAll(loadingLabel, progressBar);
        windowMainLayout.getChildren().add(vb);

        stage.show();
    }

    private ProgressBar getProgressBar() {
        ProgressBar progressBar = new ProgressBar(-1.0);
        progressBar.setPrefWidth(400);
        return progressBar;
    }

    private Label getLoadingLabel() {
        return new Label("Initializing. Please wait.");
    }

    @Override
    public void stop() {
        Platform.exit();
        System.exit(0);
    }
}
