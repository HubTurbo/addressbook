package hubturbo.updater;

import address.updater.LibraryDescriptor;
import address.updater.VersionDescriptor;
import address.util.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Unpacks all JARs required to install addressbook onto the disk.
 */
public class Installer {
    private static final String LIB_DIR = "lib";
    private static final Path MAIN_APP_FILEPATH = Paths.get(LIB_DIR, new File("resource.jar").getName());

    public void runInstall(Label label, ProgressBar progressBar) throws IOException {
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
            downloadPlatformSpecificComponents(label, progressBar);
        } catch (IOException e) {
            throw new IOException("Failed to download some components.", e);
        }
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

                // TODO: Don't extract files after first run, otherwise they might become older
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

    private void downloadPlatformSpecificComponents(Label loadingLabel, ProgressBar progressBar) throws IOException {
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
                downloadFile(libFile, downloadLink, progressBar);
            } catch (IOException e) {
                System.out.println("Failed to download library " + platformDependentLibrary.getFilename());
                throw e;
            }
        }

        System.out.println("Finished downloading platform-dependent libraries");
    }


    private void downloadFile(File targetFile, URL source, ProgressBar progressBar) throws IOException {
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
}
