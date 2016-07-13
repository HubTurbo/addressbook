package hubturbo.updater;

import address.updater.LibraryDescriptor;
import address.updater.VersionDescriptor;
import address.util.*;
import javafx.application.Platform;
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Unpacks all JARs required to run addressbook onto the disk.
 */
public class Installer {
    private static final String LIBRARY_DIR = "lib";
    private static final Path MAIN_APP_FILEPATH = Paths.get(LIBRARY_DIR, new File("resource.jar").getName());
    private static final String UPDATE_DATA_RESOURCE = "/UpdateData.json";

    public void runInstall(Label label, ProgressBar progressBar) throws IOException {
        try {
            createLibraryDir();
        } catch (IOException e) {
            throw new IOException("Failed to library directories", e);
        }

        try {
            unpackAllJarsInsideSelf(label);
        } catch (IOException e) {
            throw new IOException("Failed to unpack JARs.", e);
        }

        try {
            downloadPlatformSpecificComponents(label, progressBar);
        } catch (IOException e) {
            throw new IOException("Failed to download some components.", e);
        }
    }

    /**
     * Creates the directory to store libraries, if it hasn't been created
     *
     * @throws IOException
     */
    private void createLibraryDir() throws IOException {
        FileUtil.createDirs(new File(LIBRARY_DIR));
    }

    /**
     * Unpacks all zipped jars inside this jar, into their appropriate directories
     * @throws IOException
     */
    private void unpackAllJarsInsideSelf(Label label) throws IOException {
        System.out.println("Unpacking");

        try {
            JarFile jar = new JarFile(getSelfJarFilename()); // Installer JAR
            Platform.runLater(() -> label.setText("jar file gotten..."));
            Enumeration<JarEntry> enums = jar.entries();
            while (enums.hasMoreElements()) {
                JarEntry jarEntry = enums.nextElement();
                String fileName = jarEntry.getName();
                if (fileName.endsWith(".jar")) {
                    Path extractDest = Paths.get(fileName);


                    // Only extract file if it is not present
                    File resourceFile = extractDest.toFile();
                    if (!resourceFile.exists()) {
                        Platform.runLater(() -> label.setText("Extracting file: " + resourceFile));
                        extractJarFile(jar, jarEntry, isResourceJar(fileName) ? MAIN_APP_FILEPATH : extractDest);
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

    private boolean isResourceJar(String filename) {
        return filename.startsWith("resource") && filename.endsWith(".jar");
    }

    private String getSelfJarFilename() throws URISyntaxException {
        return FileUtil.getJarFileOfClass(this.getClass()).getName();
    }

    private void downloadPlatformSpecificComponents(Label loadingLabel, ProgressBar progressBar) throws IOException {
        System.out.println("Getting platform specific components");
        Platform.runLater(() -> loadingLabel.setText("Downloading required components. Please wait."));

        String json = FileUtil.readFromInputStream(Installer.class.getResourceAsStream(UPDATE_DATA_RESOURCE));
        VersionDescriptor versionDescriptor = JsonUtil.fromJsonString(json, VersionDescriptor.class);
        List<LibraryDescriptor> osDependentLibraries = getOsDependentLibraries(versionDescriptor, OsDetector.getOs());
        List<LibraryDescriptor> missingLibraries = getMissingLibraries(osDependentLibraries);

        int noOfMissingLibraries = missingLibraries.size();
        for (int i = 0; i < noOfMissingLibraries; i++) {
            LibraryDescriptor libraryToDownload = missingLibraries.get(i);
            final String loadingLabelString = "Downloading " + (i + 1) + "/" + noOfMissingLibraries + ": " + libraryToDownload.getFilename();
            Platform.runLater(() -> loadingLabel.setText(loadingLabelString));

            File libFile = Paths.get(LIBRARY_DIR, libraryToDownload.getFilename()).toFile();
            URL downloadLink = libraryToDownload.getDownloadLink();
            try {
                downloadFile(libFile, downloadLink, progressBar);
            } catch (IOException e) {
                System.out.println("Failed to download library " + libraryToDownload.getFilename());
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
        } catch (IOException e) {
            return false;
        }
        return libDownloadFileSize != -1 && libFile.exists() && libFile.length() == libDownloadFileSize;
    }

    private int getLibraryDownloadFileSize(URL downloadLink) throws IOException {
        URLConnection conn = downloadLink.openConnection();
        return conn.getContentLength();
    }

    private ArrayList<LibraryDescriptor> getOsDependentLibraries(VersionDescriptor versionDescriptor,
                                                                 OsDetector.Os os) {
        return versionDescriptor.getLibraries().stream()
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
                    File libFile = Paths.get(LIBRARY_DIR, libDesc.getFilename()).toFile();
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
}
