package hubturbo.updater;

import address.updater.UpdateProgressNotifier;
import address.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import commons.FileUtil;
import commons.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * This class is meant to check with the server to determine if there is a newer version of the app to update to
 *
 * Data required for the update will be downloaded in the background while the app is running,
 * and the updates will automatically be applied using a separate application only after the user closes the app
 */
public class Updater {
    public static final String UPDATE_DIR = "update";

    // --- Messages
    private static final String MSG_FAIL_DELETE_UPDATE_SPEC = "Failed to delete previous update spec file";
    private static final String MSG_FAIL_DOWNLOAD_UPDATE = "Downloading update failed";
    private static final String MSG_FAIL_CREATE_UPDATE_SPEC = "Failed to create update specification";
    private static final String MSG_FAIL_EXTRACT_JAR_UPDATER = "Failed to extract JAR installer";
    private static final String MSG_FAIL_UPDATE_NOT_SUPPORTED = "Update not supported on detected OS";
    private static final String MSG_FAIL_OBTAIN_LATEST_VERSION_DATA = "Unable to obtain latest version data. Please manually download the latest version.";
    private static final String MSG_NO_UPDATE = "There is no update";
    private static final String MSG_FAIL_READ_LATEST_VERSION = "Error reading latest version";
    private static final String MSG_NO_NEWER_VERSION = "No newer version to be downloaded";
    private static final String MSG_SKIP_DEVELOPER_ENVIRONMENT = "Developer environment detected; not performing update";
    private static final String MSG_UPDATE_FINISHED = "Update will be applied on next launch";
    // --- End of Messages

    private static final String JAR_UPDATER_RESOURCE_PATH = "updater/updateMigrator.jar";
    private static final String JAR_UPDATER_APP_PATH = UPDATE_DIR + File.separator + "updateMigrator.jar";
    private static final File DOWNLOADED_VERSIONS_FILE = new File(UPDATE_DIR + File.separator + "downloaded_versions");
    private static final String VERSION_DATA_ON_SERVER_STABLE =
            "https://raw.githubusercontent.com/HubTurbo/addressbook/stable/VersionData.json";
    private static final String VERSION_DATA_ON_SERVER_EARLY_ACCESS =
            "https://raw.githubusercontent.com/HubTurbo/addressbook/early-access/VersionData.json";
    private static final File VERSION_DESCRIPTOR_FILE = new File(UPDATE_DIR + File.separator + "VersionData.json");
    private static final String LIB_DIR = "lib" + File.separator;
    private static final String MAIN_APP_FILEPATH = LIB_DIR + "resource.jar";

    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final DependencyHistoryHandler dependencyHistoryHandler;
    private final BackupHandler backupHandler;
    private final Version currentVersion;
    private final List<Version> downloadedVersions;
    private UpdateProgressNotifier updateProgressNotifier;

    private boolean isUpdateApplicable;

    public Updater(Version currentVersion) {
        super();
        isUpdateApplicable = false;
        dependencyHistoryHandler = new DependencyHistoryHandler(currentVersion);
        backupHandler = new BackupHandler(currentVersion, dependencyHistoryHandler);
        this.currentVersion = currentVersion;
        downloadedVersions = getDownloadedVersionsFromFile(DOWNLOADED_VERSIONS_FILE);
    }

    public void start(UpdateProgressNotifier updateProgressNotifier) {
        this.updateProgressNotifier = updateProgressNotifier;
        pool.execute(backupHandler::cleanupBackups);
        pool.execute(this::checkForUpdate);
    }

    private void checkForUpdate() {
        updateProgressNotifier.sendStatusInProgress("Clearing local update specification file", -1);
        try {
            LocalUpdateSpecificationHelper.clearLocalUpdateSpecFile();
        } catch (IOException e) {
            updateProgressNotifier.sendStatusFailed(MSG_FAIL_DELETE_UPDATE_SPEC);
            return;
        }

        updateProgressNotifier.sendStatusInProgress("Getting data from server", -1);
        VersionData latestData;
        try {
            latestData = getLatestDataFromServer();
        } catch (IOException e) {
            updateProgressNotifier.sendStatusFailed(MSG_FAIL_OBTAIN_LATEST_VERSION_DATA);
            return;
        }

        Version latestVersion;
        try {
            latestVersion = getVersion(latestData);
        } catch (IllegalArgumentException e) {
            updateProgressNotifier.sendStatusFailed(MSG_FAIL_READ_LATEST_VERSION);
            return;
        }

        // Close app if wrong release channel - EA <-> stable
        assert isOnSameReleaseChannel(latestVersion) : "Error: latest version found to be in the wrong release channel";

        if (currentVersion.compareTo(latestVersion) >= 0) {
            updateProgressNotifier.sendStatusFinished(MSG_NO_NEWER_VERSION);
            return;
        }

        updateProgressNotifier.sendStatusInProgress("Collecting all update files to be downloaded", -1);
        HashMap<String, URL> filesToBeUpdated;
        try {
            filesToBeUpdated = getFilesToDownload(latestData);
        } catch (UnsupportedOperationException e) {
            updateProgressNotifier.sendStatusFailed(MSG_FAIL_UPDATE_NOT_SUPPORTED);
            return;
        }

        if (filesToBeUpdated.isEmpty()) {
            updateProgressNotifier.sendStatusFinished(MSG_NO_UPDATE);
            return;
        }

        try {
            downloadFilesToBeUpdated(new File(UPDATE_DIR), filesToBeUpdated);
        } catch (IOException e) {
            updateProgressNotifier.sendStatusFailed(MSG_FAIL_DOWNLOAD_UPDATE);
            return;
        }

        updateProgressNotifier.sendStatusInProgress("Finalizing updates", -1);

        try {
            createUpdateSpecification(filesToBeUpdated);
        } catch (IOException e) {
            updateProgressNotifier.sendStatusFailed(MSG_FAIL_CREATE_UPDATE_SPEC);
            return;
        }
        
        try {
            extractJarUpdater();
        } catch (IOException e) {
            updateProgressNotifier.sendStatusFailed(MSG_FAIL_EXTRACT_JAR_UPDATER);
            return;
        }

        updateProgressNotifier.sendStatusFinished(MSG_UPDATE_FINISHED);
        isUpdateApplicable = true;
        downloadedVersions.add(latestVersion);
    }

    /**
     * Get latest data from the server, containing version information as well as the required libraries
     */
    private VersionData getLatestDataFromServer() throws IOException {
        URL latestDataFileUrl;

        latestDataFileUrl = getLatestVersionDataUrl(currentVersion.isEarlyAccess());

        try {
            downloadFile(VERSION_DESCRIPTOR_FILE, latestDataFileUrl);
        } catch (IOException e) {
            throw new IOException("Failed to download latest data", e);
        }

        try {
            return FileUtil.deserializeObjectFromJsonFile(VERSION_DESCRIPTOR_FILE, VersionData.class);
        } catch (IOException e) {
            throw new IOException("Failed to parse data from latest data file.", e);
        }
    }

    private URL getLatestVersionDataUrl(boolean isEarlyAccess) {
        try {
            URL latestDataFileUrl;
            if (isEarlyAccess) {
                latestDataFileUrl = new URL(VERSION_DATA_ON_SERVER_EARLY_ACCESS);
            } else {
                latestDataFileUrl = new URL(VERSION_DATA_ON_SERVER_STABLE);
            }
            return latestDataFileUrl;
        } catch (MalformedURLException e) {
            assert false : "Malformed version data url";
            return null;
        }
    }

    private Version getVersion(VersionData versionData) throws IllegalArgumentException {
        try {
            return Version.fromString(versionData.getVersion());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to read version", e);
        }
    }

    private boolean isOnSameReleaseChannel(Version version) {
        return version.isEarlyAccess() == currentVersion.isEarlyAccess();
    }

    /**
     * Determines library files to download
     * Only suitable libraries for the detected operation system will be returned
     *
     * @param versionData
     * @return
     * @throws UnsupportedOperationException if OS is unsupported for updating
     */
    private HashMap<String, URL> getFilesToDownload(VersionData versionData) throws UnsupportedOperationException {
        if (OsDetector.getOs() == OsDetector.Os.UNKNOWN) {
            throw new UnsupportedOperationException("OS not supported for updating");
        }

        List<LibraryDescriptor> librariesForOs = getLibrariesForOs(versionData.getLibraries(), OsDetector.getOs());
        List<LibraryDescriptor> librariesToDownload = getLibrariesToDownload(librariesForOs);

        HashMap<String, URL> filesToBeDownloaded = getLibraryFilesDownloadLinks(librariesToDownload);
        filesToBeDownloaded.put(MAIN_APP_FILEPATH, versionData.getDownloadLinkForMainApp());

        return filesToBeDownloaded;
    }

    private List<LibraryDescriptor> getLibrariesToDownload(List<LibraryDescriptor> requiredLibraryFiles) {
        return requiredLibraryFiles.stream()
                .filter(libDesc -> !FileUtil.isFileExists(LIB_DIR + libDesc.getFileName()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Converts a list of library files into a map of filenames and their respective download urls
     *
     * @param libraryFiles
     * @return
     */
    private HashMap<String, URL> getLibraryFilesDownloadLinks(List<LibraryDescriptor> libraryFiles) {
        HashMap<String, URL> filesToBeDownloaded = new HashMap<>();
        libraryFiles.stream()
                .forEach(libDesc -> filesToBeDownloaded.put(LIB_DIR + libDesc.getFileName(),
                                                            libDesc.getDownloadLink()));
        return filesToBeDownloaded;
    }

    /**
     * Returns the list of libraries which are either universal or match the given os
     *
     * @param libraries
     * @param Os
     * @return
     */
    private List<LibraryDescriptor> getLibrariesForOs(List<LibraryDescriptor> libraries, OsDetector.Os Os) {
        return libraries.stream()
                .filter(libDesc -> libDesc.getOs() == OsDetector.Os.ANY || libDesc.getOs() == Os)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * @param updateDir directory to store downloaded updates
     * @param filesToBeUpdated files to download for update
     */
    private void downloadFilesToBeUpdated(File updateDir, HashMap<String, URL> filesToBeUpdated) throws IOException {
        if (!FileUtil.isDirExists(updateDir)) {
            Files.createDirectory(updateDir.toPath());
        }

        int totalFilesToDownload = filesToBeUpdated.keySet().size();
        int noOfFilesDownloaded = 0;

        for (String destFile : filesToBeUpdated.keySet()) {
            downloadFile(new File(updateDir.toString(), destFile), filesToBeUpdated.get(destFile));
            noOfFilesDownloaded++;
            double progress = (1.0 * noOfFilesDownloaded) / totalFilesToDownload;
            updateProgressNotifier.sendStatusInProgress("Downloading updates", progress);
        }
    }

    /**
     * Downloads a file from source url into targetFile
     *
     * Creates targetFile is it does not exist, and replaces any existing targetFile
     *
     * @param targetFile
     * @param source
     * @throws IOException
     */
    private void downloadFile(File targetFile, URL source) throws IOException {
        InputStream in = getUrlStream(source);
        createContentFile(targetFile, in);
    }

    /**
     * Extracts a file from resourcePath into targetFile
     *
     * Creates targetFile is it does not exist, and replaces any existing targetFile
     *
     * @param targetFile
     * @param resourcePath
     * @throws IOException
     */
    private void extractFile(File targetFile, String resourcePath) throws IOException {
        InputStream in = getResourceStream(resourcePath);
        createContentFile(targetFile, in);
    }

    /**
     * Writes a stream content into a target file
     *
     * Creates targetFile is it does not exist, and replaces any existing targetFile
     *
     * @param targetFile
     * @param contentStream
     * @throws IOException
     */
    private void createContentFile(File targetFile, InputStream contentStream) throws IOException {
        createFile(targetFile);
        Files.copy(contentStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private InputStream getResourceStream(String resourcePath) {
        return Updater.class.getClassLoader().getResourceAsStream(resourcePath);
    }

    private InputStream getUrlStream(URL source) throws IOException {
        return source.openStream();
    }

    private void createFile(File targetFile) throws IOException {
        FileUtil.createFile(targetFile);
    }

    private void createUpdateSpecification(HashMap<String, URL> filesToBeUpdated) throws IOException {
        List<String> listOfFileNames = filesToBeUpdated.keySet().stream().collect(Collectors.toList());
        LocalUpdateSpecificationHelper.saveLocalUpdateSpecFile(listOfFileNames);
    }

    /**
     * Extract the UpdateMigrator resource into an external jar to prepare for updating upon app closure
     * Replaces any existing UpdateMigrator found
     *
     * @throws IOException
     */
    private void extractJarUpdater() throws IOException {
        assert Updater.class.getClassLoader().getResource(JAR_UPDATER_RESOURCE_PATH) != null : "Jar installer resource cannot be found";

        File jarUpdaterFile = new File(JAR_UPDATER_APP_PATH);

        if (!jarUpdaterFile.exists() && !jarUpdaterFile.createNewFile()) {
            throw new IOException("Failed to create jar installer empty file");
        }
        try {
            extractFile(jarUpdaterFile, JAR_UPDATER_RESOURCE_PATH);
        } catch (IOException e) {
            throw new IOException("Failed to extract jar installer", e);
        }
    }

    private void writeDownloadedVersionsToFile(List<Version> downloadedVersions, File file) throws IOException {
        try {
            if (!FileUtil.isFileExists(file.toString())) {
                FileUtil.createFile(file);
            }

            FileUtil.serializeObjectToJsonFile(file, downloadedVersions);
        } catch (JsonProcessingException e) {
            throw new IOException("Failed to convert downloaded version to JSON", e);
        } catch (IOException e) {
            throw new IOException("Failed to write downloaded version to file", e);
        }
    }

    private List<Version> getDownloadedVersionsFromFile(File file) {
        try {
            return JsonUtil.fromJsonStringToList(FileUtil.readFromFile(file), Version.class);
        } catch (IOException e) {
            System.out.println("Failed to read downloaded version from file: " + e);
            return new ArrayList<>();
        }
    }

    private void applyUpdate() {
        if (!this.isUpdateApplicable) return;

        try {
            backupHandler.createAppBackup(currentVersion);
        } catch (IOException | URISyntaxException e) {
            System.out.println("Failed to create backup of app; not applying update: " + e);
            return;
        }

        try {
            writeDownloadedVersionsToFile(downloadedVersions, DOWNLOADED_VERSIONS_FILE);
        } catch (IOException e) {
            System.out.println("Error writing downloaded versions to file: " + e);
            return;
        }

        String localUpdateSpecFilepath = LocalUpdateSpecificationHelper.getLocalUpdateSpecFilepath();
        String jarUpdaterCmdArguments = String.format("--update-specification=%s --source-dir=%s", localUpdateSpecFilepath, UPDATE_DIR);
        String jarUpdaterCmd = String.format("java -jar %1$s %2$s", JAR_UPDATER_APP_PATH, jarUpdaterCmdArguments);

        System.out.println("Starting jar installer");

        try {
            Runtime.getRuntime().exec(jarUpdaterCmd);
        } catch (IOException e) {
            System.out.println("Failed to start jar installer.");
        }
    }

    public void stop() {
        applyUpdate();
    }
}
