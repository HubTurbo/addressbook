package installer;

import address.updater.model.LibraryDescriptor;
import address.updater.model.UpdateData;
import address.util.FileUtil;
import address.util.JsonUtil;
import address.util.OsDetector;
import address.util.ProgressAwareInputStream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    private static final Logger logger = LogManager.getLogger(Installer.class);
    private static final String LIB_DIR = "lib";

    private final ExecutorService pool = Executors.newSingleThreadExecutor();
    private ProgressBar progressBar;
    private Label loadingLabel;

    @Override
    public void start(Stage primaryStage) throws Exception {
        showWaitingWindow(primaryStage);

        pool.execute(() -> {
            run();
            stop();
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

    private void run() {
        try {
            createLibDir();
        } catch (IOException e) {
            logger.info("Can't create lib directory");
            e.printStackTrace();
            return;
        }

        try {
            unpackAllJarsInsideSelf();
        } catch (IOException e) {
            logger.info("Failed to unpack all JARs");
            e.printStackTrace();
            return;
        }

        downloadPlatformSpecificComponents();
        startMainApplication();
    }

    private void createLibDir() throws IOException {
        FileUtil.createDirs(new File(LIB_DIR));
    }

    private void unpackAllJarsInsideSelf() throws IOException {
        logger.info("Unpacking");

        File installerFile = new File(getSelfJarFilename()); // JAR of this class
        try (JarFile jar = new JarFile(installerFile)) {
            for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements(); ) {
                JarEntry jarEntry = enums.nextElement();

                String filename = jarEntry.getName();
                Path extractDest = Paths.get(LIB_DIR, new File(filename).getName());

                if (filename.endsWith(".jar") && jarEntry.getSize() != extractDest.toFile().length()) {
                    try (InputStream in = jar.getInputStream(jarEntry)) {
                        Files.copy(in, extractDest, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch (IOException e) {
            logger.info("UpdateManager - Failed to extract jar updater");
            throw e;
        }

        logger.info("Finished Unpacking");
    }

    private String getSelfJarFilename() {
        return FileUtil.getJarFileOfClass(this.getClass()).getName();
    }

    private void downloadPlatformSpecificComponents() {
        logger.info("Getting platform specific components");

        String json = FileUtil.readFromInputStream(Installer.class.getResourceAsStream("/UpdateData.json"));

        UpdateData updateData;

        try {
            updateData = JsonUtil.fromJsonString(json, UpdateData.class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        List<LibraryDescriptor> platformDependentLibraries =  updateData.getLibraries().stream()
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
                    logger.info("Library already exists - " + platformDependentLibrary.getFilename());
                    break;
                }
            } catch (IOException e) {
                logger.info("Failed to get size of library; will proceed to downloading it - " +
                        platformDependentLibrary.getFilename());
                e.printStackTrace();
            }
            
            Platform.runLater(() -> loadingLabel.setText("Initializing. Downloading required components. Please wait."));

            try {
                downloadFile(libFile, downloadLink);
            } catch (IOException e) {
                logger.info("Failed to download library - " + platformDependentLibrary.getFilename());
                e.printStackTrace();
                return;
            }
        }

        logger.info("Finished downloading platform dependent libraries");
    }

    private void startMainApplication() {
        logger.info("Starting main application");

        String classPath = File.pathSeparator + "lib" + File.separator + "*";

        String command = String.format("java -cp %s address.MainApp", classPath);

        try {
            Runtime.getRuntime().exec(command, null, new File(System.getProperty("user.dir")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("Main application launched");
    }

    private void downloadFile(File targetFile, URL source) throws IOException {
        try (InputStream in = source.openStream()) {
            if (!FileUtil.createFile(targetFile)) {
                logger.info("File already exists; file will be replaced");
            }

            URLConnection conn = source.openConnection();
            ProgressAwareInputStream inWithProgress = new ProgressAwareInputStream(in, conn.getContentLength());
            inWithProgress.setOnProgressListener(prog -> Platform.runLater(() -> progressBar.setProgress(prog)));

            Files.copy(inWithProgress, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.info(String.format("Installer - Failed to download %s", targetFile.toString()));
            throw e;
        }
    }
}
