package hubturbo.installer;

import address.util.LibraryDescriptor;
import address.util.VersionData;
import address.util.*;
import commons.FileUtil;
import commons.JsonUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Installer JAR will contain all of addressbook's required JARs (libraries and dependencies) and
 * the main application JAR.
 */
public class Installer extends Application {
    private static final String LIBRARY_DIR = "lib";
    private static final Path LAUNCHER_FILE_PATH = Paths.get("launcher.jar");
    private static final String VERSION_DATA_RESOURCE = "/VersionData.json";
    private static final String VERSION_DATA = "VersionData.json";

    private final ExecutorService pool = Executors.newSingleThreadExecutor();
    private ProgressBar progressBar;
    private Label loadingLabel;

    @Override
    public void start(Stage primaryStage) throws Exception {
        showWaitingWindow(primaryStage);
        Platform.runLater(() -> loadingLabel.setText("Installing..."));
        pool.execute(() -> {
            try {
                if (shouldRunInstaller()) runInstall();
                stop();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void stop() {
        Platform.exit();
        System.exit(0);
    }

    private void runInstall() throws IOException {
        Platform.runLater(() -> loadingLabel.setText("Creating lib dir"));
        try {
            createLibraryDir();
        } catch (IOException e) {
            throw new IOException("Failed to library directories", e);
        }

        Platform.runLater(() -> loadingLabel.setText("Extracting missing jar files"));
        try {
            extractMissingJarFiles();
        } catch (IOException e) {
            throw new IOException("Failed to extract JARs.", e);
        }

        try {
            downloadPlatformSpecificComponents();
        } catch (IOException e) {
            throw new IOException("Failed to download some components.", e);
        }

        try {
            runLauncher();
        } catch (IOException e) {
            throw new IOException("Failed to start launcher.", e);
        }
    }

    private void runLauncher() throws IOException {
        try {
            System.out.println("Starting launcher");
            String command = "java -jar addressbook.jar";
            Runtime.getRuntime().exec(command, null, new File(System.getProperty("user.dir")));
            System.out.println("Launcher started");
        } catch (IOException e) {
            throw new IOException("Error starting launcher", e);
        }
    }

    /**
     * Determines if installer should be run
     *
     * Installer should only be run only if there is no existing installation or existing installation has the
     * matching version as the installer
     *
     * Assumes that the application is not installed yet if version data file cannot be found
     *
     * @return
     * @throws IOException
     */
    private boolean shouldRunInstaller() throws IOException {
        Optional<VersionData> currentVersionData = getCurrentVersionData();
        if (!currentVersionData.isPresent()) return true;
        VersionData packedVersionData = getPackedVersionData();

        Version currentVersion = Version.fromString(currentVersionData.get().getVersion());
        Version packedVersion = Version.fromString(packedVersionData.getVersion());
        return packedVersion.compareTo(currentVersion) == 0;
    }


    private Optional<VersionData> getCurrentVersionData() throws IOException {
        File versionDataFile = new File(VERSION_DATA);
        if (!versionDataFile.exists()) return Optional.empty();
        return Optional.of(JsonUtil.fromJsonString(FileUtil.readFromFile(versionDataFile), VersionData.class));
    }

    private VersionData getPackedVersionData() throws IOException {
        String json = FileUtil.readFromInputStream(Installer.class.getResourceAsStream(VERSION_DATA_RESOURCE));
        return JsonUtil.fromJsonString(json, VersionData.class);
    }

    private void showWaitingWindow(Stage stage) {
        stage.setTitle("Installer");
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

    /**
     * Creates the directory and all its parent directories to store libraries, if they haven't been created
     *
     * @throws IOException
     */
    private void createLibraryDir() throws IOException {
        FileUtil.createDirs(new File(LIBRARY_DIR));
    }

    /**
     * Unpacks zipped jars inside this jar, into their appropriate directories,
     * if they are not found in their destination
     * @throws IOException
     */
    private void extractMissingJarFiles() throws IOException {
        System.out.println("Unpacking");

        try {
            JarFile jar = new JarFile(getSelfJarFilename()); // Installer JAR
            Enumeration<JarEntry> enums = jar.entries();
            while (enums.hasMoreElements()) {

                JarEntry jarEntry = enums.nextElement();
                String fileName = jarEntry.getName();
                if (fileName.endsWith(".jar") || fileName.equals("VersionData.json")) {
                    Path extractDest = Paths.get(fileName);

                    System.out.println("Extract dest: " + extractDest);
                    // Only extract file if it is not present
                    File resourceFile = extractDest.toFile();
                    // TODO: installer should not blindly extract libraries since it might be outdated
                    // outdated installer with outdated libraries could lead to extracting unnecessary library files
                    // on each start-up
                    if (!resourceFile.exists()) {
                        Platform.runLater(() -> loadingLabel.setText("Extracting file: " + resourceFile));
                        extractJarFile(jar, jarEntry, isLauncherJar(fileName) ? LAUNCHER_FILE_PATH : extractDest);
                    }
                }
            }
        } catch (URISyntaxException e) {
            System.out.println("Failed to obtain self JAR");
            throw new IOException("Failed to obtain self JAR", e);
        }

        System.out.println("Finished Unpacking");
    }

    private void extractJarFile(JarFile selfJar, JarEntry jarEntry, Path extractDest) throws IOException {
        InputStream in = selfJar.getInputStream(jarEntry);
        Files.copy(in, extractDest, StandardCopyOption.REPLACE_EXISTING);
    }

    private boolean isLauncherJar(String filename) {
        return filename.startsWith("launcher") && filename.endsWith(".jar");
    }

    private String getSelfJarFilename() throws URISyntaxException {
        return FileUtil.getJarFileOfClass(this.getClass()).getName();
    }

    private void downloadPlatformSpecificComponents() throws IOException {
        System.out.println("Getting platform specific components");
        Platform.runLater(() -> loadingLabel.setText("Downloading required components. Please wait."));

        String json = FileUtil.readFromInputStream(Installer.class.getResourceAsStream(VERSION_DATA_RESOURCE));
        VersionData versionData = JsonUtil.fromJsonString(json, VersionData.class);
        List<LibraryDescriptor> osDependentLibraries = getOsDependentLibraries(versionData, OsDetector.getOs());
        List<LibraryDescriptor> missingLibraries = getMissingLibraries(osDependentLibraries);

        int noOfMissingLibraries = missingLibraries.size();
        for (int i = 0; i < noOfMissingLibraries; i++) {
            LibraryDescriptor libraryToDownload = missingLibraries.get(i);
            final String loadingLabelString = "Downloading " + (i + 1) + "/" + noOfMissingLibraries + ": " + libraryToDownload.getFileName();
            Platform.runLater(() -> loadingLabel.setText(loadingLabelString));

            File libFile = Paths.get(LIBRARY_DIR, libraryToDownload.getFileName()).toFile();
            URL downloadLink = libraryToDownload.getDownloadLink();
            try {
                downloadFile(libFile, downloadLink, progressBar);
            } catch (IOException e) {
                System.out.println("Failed to download library " + libraryToDownload.getFileName());
                throw e;
            }
        }

        System.out.println("Finished downloading platform-dependent libraries");
    }

    /**
     * Returns whether libFile is an existing library
     * It is considered an existing library only if a file with the same name and size exists
     *
     * @param libFile
     * @param downloadLink
     * @return
     */
    private boolean isExistingLibrary(File libFile, URL downloadLink) {
        int libDownloadFileSize;
        try {
            libDownloadFileSize = getLibraryDownloadFileSize(downloadLink);
            if (libDownloadFileSize == -1) return false;
        } catch (IOException e) {
            return false;
        }
        return libFile.exists() && libFile.length() == libDownloadFileSize;
    }

    private int getLibraryDownloadFileSize(URL downloadLink) throws IOException {
        URLConnection conn = downloadLink.openConnection();
        return conn.getContentLength();
    }

    private ArrayList<LibraryDescriptor> getOsDependentLibraries(VersionData versionData,
                                                                 OsDetector.Os os) {
        return versionData.getLibraries().stream()
                .filter(libDesc -> libDesc.getOs() == os)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Filters the list of libraries for only missing libraries
     * @param requiredLibraries
     * @return
     */
    private ArrayList<LibraryDescriptor> getMissingLibraries(List<LibraryDescriptor> requiredLibraries) {
        return requiredLibraries.stream()
                .filter(libDesc -> {
                    File libFile = Paths.get(LIBRARY_DIR, libDesc.getFileName()).toFile();
                    return !isExistingLibrary(libFile, libDesc.getDownloadLink());
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }


    private void downloadFile(File targetFile, URL source, ProgressBar progressBar) throws IOException {
        try (InputStream in = source.openStream()) {
            if (!FileUtil.createFile(targetFile)) {
                System.out.println("File already exists; file will be replaced");
            }

            URLConnection conn = source.openConnection();
            ProgressAwareInputStream inputStreamWithProgress = new ProgressAwareInputStream(in, conn.getContentLength());
            inputStreamWithProgress.setOnProgressListener(prog -> Platform.runLater(() -> progressBar.setProgress(prog)));

            Files.copy(inputStreamWithProgress, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Failed to download " + targetFile.toString());
            throw e;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}