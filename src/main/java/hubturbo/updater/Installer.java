package hubturbo.updater;

import address.updater.LibraryDescriptor;
import address.updater.VersionDescriptor;
import address.util.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Unpack all JARs required to run addressbook to the disk.
 *
 * Installer JAR will contain all of addressbook required JARs (libraries and dependencies) and
 * the main application JAR.
 */
public class Installer extends Application {
    private static final String ERROR_INSTALL = "Failed to install";
    private static final String ERROR_RUNNING = "Failed to run application";
    private static final String ERROR_TRY_AGAIN = "Please try again, or contact developer if it keeps failing.";
    private static final String LIB_DIR = "lib";
    private static final Path MAIN_APP_FILEPATH = Paths.get(LIB_DIR, new File("resource.jar").getName());

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
                showErrorDialogAndQuit(ERROR_INSTALL, e.getMessage(), ERROR_TRY_AGAIN);
            }
        });
    }

    private void showWaitingWindow(Stage stage) {
        stage.setTitle("Initializing");
        VBox windowMainLayout = new VBox();
        Group root = new Group();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        scene.setRoot(windowMainLayout);

        loadingLabel = new Label("Initializing. Please wait.");

        progressBar = new ProgressBar(-1.0);
        progressBar.setPrefWidth(400);

        final VBox vb = new VBox();
        vb.setSpacing(30);
        vb.setPadding(new Insets(40));
        vb.setAlignment(Pos.CENTER);
        vb.getChildren().addAll(loadingLabel, progressBar);
        windowMainLayout.getChildren().add(vb);

        stage.show();
    }

    @Override
    public void stop() {
        Platform.exit();
        System.exit(0);
    }

    private void run() throws IOException {
        try {
            createLibDir();
        } catch (IOException e) {
            System.out.println("Can't create lib directory");
            throw new IOException("Failed to create directories", e);
        }

        try {
            unpackAllJarsInsideSelf();
        } catch (IOException e) {
            System.out.println("Failed to unpack all JARs");
            throw new IOException("Failed to unpack files.", e);
        } catch (URISyntaxException e) {
            System.out.println("Failed to get self JAR");
            throw new IOException("Failed to unpack files.", e);
        }

        try {
            downloadPlatformSpecificComponents();
        } catch (IOException e) {
            throw new IOException("Failed to download some components.", e);
        }

        try {
            startMainApplication();
        } catch (IOException e) {
            throw new IOException(ERROR_RUNNING, e);
        }

        stop();
    }

    private void createLibDir() throws IOException {
        FileUtil.createDirs(new File(LIB_DIR));
    }

    private void unpackAllJarsInsideSelf() throws IOException, URISyntaxException {
        System.out.println("Unpacking");

        File installerFile = new File(getSelfJarFilename()); // JAR of this class
        try (JarFile jar = new JarFile(installerFile)) {
            for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements(); ) {
                JarEntry jarEntry = enums.nextElement();

                String filename = jarEntry.getName();
                Path extractDest = Paths.get(LIB_DIR, new File(filename).getName());

                // For MainApp resource, only extract if it is not present
                if (filename.startsWith("resource") && filename.endsWith(".jar")) {
                    if (!MAIN_APP_FILEPATH.toFile().exists()) {
                        try (InputStream in = jar.getInputStream(jarEntry)) {
                            Files.copy(in, MAIN_APP_FILEPATH, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                    continue;
                }

                // For other JARs, extract if existing files are of different sizes
                if (filename.endsWith(".jar") && jarEntry.getSize() != extractDest.toFile().length()) {
                    try (InputStream in = jar.getInputStream(jarEntry)) {
                        Files.copy(in, extractDest, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to extract libraries");
            throw e;
        }

        System.out.println("Finished Unpacking");
    }

    private String getSelfJarFilename() throws URISyntaxException {
        return FileUtil.getJarFileOfClass(this.getClass()).getName();
    }

    private void downloadPlatformSpecificComponents() throws IOException {
        System.out.println("Getting platform specific components");

        String json = FileUtil.readFromInputStream(Installer.class.getResourceAsStream("/UpdateData.json"));

        VersionDescriptor versionDescriptor;

        try {
            versionDescriptor = JsonUtil.fromJsonString(json, VersionDescriptor.class);
        } catch (IOException e) {
            throw e;
        }

        List<LibraryDescriptor> platformDependentLibraries =  versionDescriptor.getLibraries().stream()
                .filter(libDesc -> libDesc.getOs() == OsDetector.getOs())
                .collect(Collectors.toList());

        for (LibraryDescriptor platformDependentLibrary : platformDependentLibraries) {
            URL downloadLink = platformDependentLibrary.getDownloadLink();

            File libFile = Paths.get("lib", platformDependentLibrary.getFilename()).toFile();

            try {
                URLConnection conn = downloadLink.openConnection();
                int libDownloadFileSize = conn.getContentLength();
                if (libDownloadFileSize != -1 && FileUtil.isFileExists(libFile.toString()) &&
                        libFile.length() == libDownloadFileSize) {
                    System.out.println("Library already exists: " + platformDependentLibrary.getFilename());
                    break;
                }
            } catch (IOException e) {
                System.out.println("Failed to get size of library; will proceed to download it: " +
                        platformDependentLibrary.getFilename());
            }
            
            Platform.runLater(() -> loadingLabel.setText("Downloading required components. Please wait."));

            try {
                downloadFile(libFile, downloadLink);
            } catch (IOException e) {
                System.out.println("Failed to download library " + platformDependentLibrary.getFilename());
                throw e;
            }
        }

        System.out.println("Finished downloading platform-dependent libraries");
    }

    private void startMainApplication() throws IOException {
        System.out.println("Starting main application");

        String classPath = File.pathSeparator + "lib" + File.separator + "*";

        String command = String.format("java -ea -cp %s address.MainApp", classPath);

        Runtime.getRuntime().exec(command, null, new File(System.getProperty("user.dir")));

        System.out.println("Main application launched");
    }

    private void downloadFile(File targetFile, URL source) throws IOException {
        try (InputStream in = source.openStream()) {
            if (!FileUtil.createFile(targetFile)) {
                System.out.println("File already exists; file will be replaced");
            }

            URLConnection conn = source.openConnection();
            ProgressAwareInputStream inWithProgress = new ProgressAwareInputStream(in, conn.getContentLength());
            inWithProgress.setOnProgressListener(prog -> Platform.runLater(() -> progressBar.setProgress(prog)));

            Files.copy(inWithProgress, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Failed to download " + targetFile.toString());
            throw e;
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
}
