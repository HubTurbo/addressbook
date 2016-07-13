package address.updater;

import address.events.UpdaterFailedEvent;
import address.events.UpdaterFinishedEvent;
import address.events.UpdaterInProgressEvent;
import address.main.ComponentManager;
import address.storage.StorageManager;
import address.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;

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
public class UpdateManager extends ComponentManager {
    public static final String UPDATE_DIR = "update";
    private static final AppLogger logger = LoggerManager.getLogger(UpdateManager.class);

    // --- Messages
    private static final String MSG_FAIL_DELETE_UPDATE_SPEC = "Failed to delete previous update spec file";
    private static final String MSG_FAIL_DOWNLOAD_UPDATE = "Downloading update failed";
    private static final String MSG_FAIL_CREATE_UPDATE_SPEC = "Failed to create update specification";
    private static final String MSG_FAIL_EXTRACT_JAR_UPDATER = "Failed to extract JAR updater";
    private static final String MSG_FAIL_UPDATE_NOT_SUPPORTED = "Update not supported on detected OS";
    private static final String MSG_FAIL_OBTAIN_DATA = "Error obtaining latest data";
    private static final String MSG_NO_UPDATE = "There is no update";
    private static final String MSG_FAIL_READ_LATEST_VERSION = "Error reading latest version";
    private static final String MSG_NO_NEWER_VERSION = "No newer version to be downloaded";
    private static final String MSG_SKIP_DEVELOPER_ENVIRONMENT = "Developer environment detected; not performing update";
    private static final String MSG_UPDATE_FINISHED = "Update will be applied on next launch";
    // --- End of Messages

    private static final String JAR_UPDATER_RESOURCE_PATH = "updater/jarUpdater.jar";
    private static final String JAR_UPDATER_APP_PATH = UPDATE_DIR + File.separator + "jarUpdater.jar";
    private static final File DOWNLOADED_VERSIONS_FILE = new File(UPDATE_DIR + File.separator + "downloaded_versions");
    private static final String VERSION_DESCRIPTOR_ON_SERVER_STABLE =
            "https://raw.githubusercontent.com/HubTurbo/addressbook/stable/UpdateData.json";
    private static final String VERSION_DESCRIPTOR_ON_SERVER_EARLY_ACCESS =
            "https://raw.githubusercontent.com/HubTurbo/addressbook/early-access/UpdateData.json";
    private static final File VERSION_DESCRIPTOR_FILE = new File(UPDATE_DIR + File.separator + "UpdateData.json");
    private static final String LIB_DIR = "lib" + File.separator;
    private static final String MAIN_APP_FILEPATH = LIB_DIR + "resource.jar";

    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final DependencyHistoryHandler dependencyHistoryHandler;
    private final BackupHandler backupHandler;
    private final Version currentVersion;
    private final List<Version> downloadedVersions;

    private boolean isUpdateApplicable;

    public UpdateManager(Version currentVersion) {
        super();
        this.isUpdateApplicable = false;
        dependencyHistoryHandler = new DependencyHistoryHandler(currentVersion);
        backupHandler = new BackupHandler(currentVersion, dependencyHistoryHandler);
        this.currentVersion = currentVersion;
        downloadedVersions = getDownloadedVersionsFromFile(DOWNLOADED_VERSIONS_FILE);
    }

    public void start() {
        logger.info("Starting update manager.");
        pool.execute(backupHandler::cleanupBackups);
        pool.execute(this::checkForUpdate);
    }

    private void checkForUpdate() {
        if (!ManifestFileReader.isRunFromJar()) {
            logger.info(MSG_SKIP_DEVELOPER_ENVIRONMENT);
            raise(new UpdaterFinishedEvent(MSG_SKIP_DEVELOPER_ENVIRONMENT));
            return;
        }

        raise(new UpdaterInProgressEvent("Clearing local update specification file", -1));
        try {
            LocalUpdateSpecificationHelper.clearLocalUpdateSpecFile();
        } catch (IOException e) {
            raise(new UpdaterFailedEvent(MSG_FAIL_DELETE_UPDATE_SPEC));
            logger.debug("Failed to delete existing specification file: {}", e);
            return;
        }

        raise(new UpdaterInProgressEvent("Getting data from server", -1));
        VersionDescriptor latestData;
        try {
            latestData = getLatestDataFromServer();
        } catch (IOException e) {
            raise(new UpdaterFailedEvent(MSG_FAIL_OBTAIN_DATA));
            logger.debug("Failed to obtain data from server: {}", e);
            return;
        }

        Version latestVersion;
        try {
            latestVersion = getLatestVersion(latestData);
        } catch (IllegalArgumentException e) {
            raise(new UpdaterFailedEvent(MSG_FAIL_READ_LATEST_VERSION));
            logger.fatal("Failed to obtain latest version data from downloaded data: {}", e);
            return;
        }

        // Close app if wrong release channel - EA <-> stable
        assert isOnSameReleaseChannel(latestVersion);

        // No newer version to update to
        if (currentVersion.compareTo(latestVersion) >= 0) {
            raise(new UpdaterFinishedEvent(MSG_NO_NEWER_VERSION));
            logger.debug(MSG_NO_NEWER_VERSION);
            return;
        }

        raise(new UpdaterInProgressEvent("Collecting all update files to be downloaded", -1));
        HashMap<String, URL> filesToBeUpdated;
        try {
            filesToBeUpdated = collectAllUpdateFilesToBeDownloaded(latestData);
        } catch (UnsupportedOperationException e) {
            raise(new UpdaterFailedEvent(MSG_FAIL_UPDATE_NOT_SUPPORTED));
            logger.debug("Update on detected OS is not supported: {}", e);
            return;
        }

        if (filesToBeUpdated.isEmpty()) {
            raise(new UpdaterFinishedEvent(MSG_NO_UPDATE));
            logger.debug(MSG_NO_UPDATE);
            return;
        }

        try {
            downloadFilesToBeUpdated(new File(UPDATE_DIR), filesToBeUpdated);
        } catch (IOException e) {
            raise(new UpdaterFailedEvent(MSG_FAIL_DOWNLOAD_UPDATE));
            logger.debug(MSG_FAIL_DOWNLOAD_UPDATE);
            return;
        }

        raise(new UpdaterInProgressEvent("Finalizing updates", -1));

        try {
            createUpdateSpecification(filesToBeUpdated);
        } catch (IOException e) {
            raise(new UpdaterFailedEvent(MSG_FAIL_CREATE_UPDATE_SPEC));
            logger.debug("Failed to create update specification file: {}", e);
            return;
        }
        
        try {
            extractJarUpdater();
        } catch (IOException e) {
            raise(new UpdaterFailedEvent(MSG_FAIL_EXTRACT_JAR_UPDATER));
            logger.debug("Failed to extract jar updater: {}", e);
            return;
        }

        raise(new UpdaterFinishedEvent(MSG_UPDATE_FINISHED));
        isUpdateApplicable = true;
        downloadedVersions.add(latestVersion);
    }

    /**
     * Get latest data from the server, containing version information as well as the required libraries
     */
    private VersionDescriptor getLatestDataFromServer() throws IOException {
        URL latestDataFileUrl;

        latestDataFileUrl = getLatestVersionDescriptorUrl(currentVersion.isEarlyAccess());

        try {
            downloadFile(VERSION_DESCRIPTOR_FILE, latestDataFileUrl);
        } catch (IOException e) {
            throw new IOException("Failed to download latest data", e);
        }

        try {
            return StorageManager.deserializeObjectFromJsonFile(VERSION_DESCRIPTOR_FILE, VersionDescriptor.class);
        } catch (IOException e) {
            throw new IOException("Failed to parse data from latest data file.", e);
        }
    }

    private URL getLatestVersionDescriptorUrl(boolean isEarlyAccess) {
        try {
            URL latestDataFileUrl;
            if (isEarlyAccess) {
                latestDataFileUrl = new URL(VERSION_DESCRIPTOR_ON_SERVER_EARLY_ACCESS);
            } else {
                latestDataFileUrl = new URL(VERSION_DESCRIPTOR_ON_SERVER_STABLE);
            }
            return latestDataFileUrl;
        } catch (MalformedURLException e) {
            assert false : "Malformed version descriptor url";
            return null;
        }
    }

    private Version getLatestVersion(VersionDescriptor versionDescriptor) throws IllegalArgumentException {
        try {
            return Version.fromString(versionDescriptor.getVersion());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to read latest version", e);
        }
    }

    private boolean isOnSameReleaseChannel(Version version) {
        return version.isEarlyAccess() == currentVersion.isEarlyAccess();
    }

    /**
     * Determines os-dependent library to download
     * @param versionDescriptor
     * @return
     * @throws UnsupportedOperationException if OS is unsupported for updating
     */
    private HashMap<String, URL> collectAllUpdateFilesToBeDownloaded(VersionDescriptor versionDescriptor) throws UnsupportedOperationException {
        OsDetector.Os machineOs = OsDetector.getOs();

        if (machineOs == OsDetector.Os.UNKNOWN) {
            throw new UnsupportedOperationException("OS not supported for updating");
        }

        HashMap<String, URL> filesToBeDownloaded = new HashMap<>();

        filesToBeDownloaded.put(MAIN_APP_FILEPATH, versionDescriptor.getDownloadLinkForMainApp());

        versionDescriptor.getLibraries().stream()
                .filter(libDesc -> libDesc.getOs() == OsDetector.Os.ANY || libDesc.getOs() == OsDetector.getOs())
                .filter(libDesc -> !FileUtil.isFileExists(LIB_DIR + libDesc.getFilename()))
                .forEach(libDesc -> filesToBeDownloaded.put(LIB_DIR + libDesc.getFilename(),
                                                            libDesc.getDownloadLink()));

        return filesToBeDownloaded;
    }

    /**
     * @param updateDir directory to store downloaded updates
     */
    private void downloadFilesToBeUpdated(File updateDir, HashMap<String, URL> filesToBeUpdated) throws IOException {
        if (!FileUtil.isDirExists(updateDir)) {
            Files.createDirectory(updateDir.toPath());
        }

        int noOfFilesTobeDownloaded = filesToBeUpdated.keySet().size();
        int noOfFilesDownloaded = 0;

        for (String destFile : filesToBeUpdated.keySet()) {
            downloadFile(new File(updateDir.toString(), destFile), filesToBeUpdated.get(destFile));
            noOfFilesDownloaded++;
            double progress = (1.0 * noOfFilesDownloaded) / noOfFilesTobeDownloaded;
            raise(new UpdaterInProgressEvent("Downloading updates", progress));
        }
    }

    private void downloadFile(File targetFile, URL source) throws IOException {
        try (InputStream in = source.openStream()) {
            if (!FileUtil.createFile(targetFile)) {
                logger.debug("File '{}' already exists", targetFile.getName());
            }
            Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Failed to download update for " + targetFile, e);
        }
    }

    private void createUpdateSpecification(HashMap<String, URL> filesToBeUpdated) throws IOException {
        LocalUpdateSpecificationHelper.saveLocalUpdateSpecFile(
                filesToBeUpdated.keySet().stream().collect(Collectors.toList())
        );
    }

    /**
     * Extract the JarUpdater resource into an external jar to prepare for updating upon app closure
     * Replaces any existing JarUpdater found
     *
     * @throws IOException
     */
    private void extractJarUpdater() throws IOException {
        assert UpdateManager.class.getClassLoader().getResource(JAR_UPDATER_RESOURCE_PATH) != null : "Jar updater resource cannot be found";

        File jarUpdaterFile = new File(JAR_UPDATER_APP_PATH);

        if (!jarUpdaterFile.exists() && !jarUpdaterFile.createNewFile()) {
            throw new IOException("Failed to create jar updater empty file");
        }

        try (InputStream in = UpdateManager.class.getClassLoader().getResourceAsStream(JAR_UPDATER_RESOURCE_PATH)) {
            Files.copy(in, jarUpdaterFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Failed to extract jar updater", e);
        }
    }

    private void writeDownloadedVersionsToFile(List<Version> downloadedVersions, File file) throws IOException {
        try {
            if (FileUtil.isFileExists(file.toString())) {
                FileUtil.createFile(file);
            }

            StorageManager.serializeObjectToJsonFile(file, downloadedVersions);
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
            logger.warn("Failed to read downloaded version from file: {}", e);
            return new ArrayList<>();
        }
    }

    public void applyUpdate() {
        if (!ManifestFileReader.isRunFromJar() || !this.isUpdateApplicable) {
            return;
        }

        try {
            backupHandler.createAppBackup(currentVersion);
        } catch (IOException | URISyntaxException e) {
            logger.warn("Failed to create backup of app; not applying update: {}", e);
            return;
        }

        try {
            writeDownloadedVersionsToFile(downloadedVersions, DOWNLOADED_VERSIONS_FILE);
        } catch (IOException e) {
            logger.warn("Error writing downloaded versions to file: {}", e);
            return;
        }

        String jarUpdaterAppPath = JAR_UPDATER_APP_PATH;
        String localUpdateSpecFilepath = LocalUpdateSpecificationHelper.getLocalUpdateSpecFilepath();
        String jarUpdaterCmdArguments = String.format("--update-specification=%s --source-dir=%s", localUpdateSpecFilepath, UPDATE_DIR);
        String jarUpdaterCmd = String.format("java -jar %1$s %2$s", jarUpdaterAppPath, jarUpdaterCmdArguments);

        logger.info("Starting jar updater with command: {}", jarUpdaterCmd);

        try {
            Runtime.getRuntime().exec(jarUpdaterCmd);
        } catch (IOException e) {
            logger.debug("Failed to start jar updater: {}", e);
        }
    }

    public void stop() {
        applyUpdate();
    }
}
