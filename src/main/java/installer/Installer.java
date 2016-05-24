package installer;

import address.util.FileUtil;
import address.util.OsDetector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Unpack all JARs required to run addressbook to the disk.
 *
 * Installer JAR will contain all of addressbook required JARs (libraries and dependencies) and
 * the main application JAR.
 */
public class Installer extends Application {

    private static final String LIB_DIR = "lib";

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO display loading screen
        run();
        stop();
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
            System.out.println("Can't create lib directory");
            e.printStackTrace();
            return;
        }

        try {
            unpackAllJarsInsideSelf();
        } catch (IOException e) {
            System.out.println("Failed to unpack all JARs");
            e.printStackTrace();
            return;
        }

        downloadPlatformSpecificJxBrowser();
        startMainApplication();
    }

    private void createLibDir() throws IOException {
        FileUtil.createDirs(new File(LIB_DIR));
    }

    private void unpackAllJarsInsideSelf() throws IOException {
        System.out.println("Unpacking");

        File installerFile = new File(getSelfJarFilename()); // JAR of this class
        try (JarFile jar = new JarFile(installerFile)) {
            for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements(); ) {
                JarEntry jarEntry = enums.nextElement();

                String filename = jarEntry.getName();
                Path extractDest = Paths.get(LIB_DIR, new File(filename).getName());

                if (filename.endsWith(".jar") && jarEntry.getSize() != extractDest.toFile().length()) {
                    try (InputStream in = jar.getInputStream(jarEntry)) {
                        Files.copy(in, extractDest, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw e;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("UpdateManager - Failed to extract jar updater");
            throw e;
        }

        System.out.println("Finished Unpacking");
    }

    private String getSelfJarFilename() {
        return new File(Installer.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
    }

    private void downloadPlatformSpecificJxBrowser() {
        System.out.println("Getting Jx Browser");

        String jxBrowserDownloadLink = "http://maven.teamdev.com/repository/products/com/teamdev/jxbrowser/";

        if (OsDetector.isOnWindows()) {
            jxBrowserDownloadLink += "jxbrowser-win/6.4/jxbrowser-win-6.4.jar";
        } else if (OsDetector.isOnMac()) {
            jxBrowserDownloadLink += "jxbrowser-mac/6.4/jxbrowser-mac-6.4.jar";
        } else if (OsDetector.isOn32BitsLinux()) {
            jxBrowserDownloadLink += "jxbrowser-linux32/6.4/jxbrowser-linux32-6.4.jar";
        } else if (OsDetector.isOn64BitsLinux()) {
            jxBrowserDownloadLink += "jxbrowser-linux64/6.4/jxbrowser-linux64-6.4.jar";
        } else {
            System.out.println("Unknown OS");
        }

        // TODO download from jxBrowserDownloadLink
        URL downloadLink;
        try {
            downloadLink = new URL(jxBrowserDownloadLink);
        } catch (MalformedURLException e) {
            System.out.println("JxBrowser download link is malformed");
            e.printStackTrace();
            return;
        }

        try {
            String jxBrowserFilename = Paths.get(downloadLink.toString()).getFileName().toString();
            File jxBrowserFile = Paths.get("lib", jxBrowserFilename).toFile();
            if (!FileUtil.isFileExists(jxBrowserFile.toString())) {
                downloadFile(jxBrowserFile, downloadLink);
            }
        } catch (IOException e) {
            System.out.println("Failed to download JxBrowser");
            e.printStackTrace();
            return;
        }

        System.out.println("Has gotten Jx Browser");
    }

    private void startMainApplication() {
        System.out.println("Starting main application");

        String classPath;

        if (OsDetector.isOnWindows()) {
            classPath = ";lib/*"; // untested
        } else {
            classPath = ":lib/*";
        }

        String command = String.format("java -cp %s address.MainApp", classPath);

        try {
            Runtime.getRuntime().exec(command, null, new File(System.getProperty("user.dir")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Main application launched");
    }

    private void downloadFile(File targetFile, URL source) throws IOException {
        try (InputStream in = source.openStream()) {
            if (!FileUtil.createFile(targetFile)) {
                throw new IOException("Error creating new file.");
            }
            Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println(String.format("Installer - Failed to download update for %s",
                    targetFile.toString()));
            throw e;
        }
    }
}
