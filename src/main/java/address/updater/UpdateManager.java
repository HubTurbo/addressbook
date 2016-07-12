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
import java.util.Optional;
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
    private static final String MSG_JAR_UPDATER_MISSING = "JAR Updater is missing";
    private static final String MSG_FAIL_MAIN_APP_URL = "Main app download link is broken";
    private static final String MSG_NO_LATEST_DATA = "There is no latest data to be processed";
    private static final String MSG_NO_UPDATE = "There is no update";
    private static final String MSG_NO_VERSION_AVAILABLE = "No version to be downloaded";
    private static final String MSG_NO_NEWER_VERSION = "No newer version to be downloaded";
    private static final String MSG_DIFF_CHANNEL = "VersionDescriptor is for wrong release channel - contact developer";
    // --- End of Messages

    private static final String JAR_UPDATER_RESOURCE_PATH = "updater/jarUpdater.jar";
    private static final String JAR_UPDATER_APP_PATH = UPDATE_DIR + File.separator + "jarUpdater.jar";
    private static final File DOWNLOADED_VERSIONS_FILE = new File(UPDATE_DIR + File.separator + "downloaded_versions");
    private static final String VERSION_DESCRIPTOR_ON_SERVER_STABLE =
            "https://raw.githubusercontent.com/HubTurbo/addressbook/stable/UpdateData.json";
    private static final String VERSION_DESCRIPTOR_ON_SERVER_EARLY =
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
        raise(new UpdaterInProgressEvent("Clearing local update specification file", -1));
        try {
            LocalUpdateSpecificationHelper.clearLocalUpdateSpecFile();
        } catch (IOException e) {
            raise(new UpdaterFailedEvent(MSG_FAIL_DELETE_UPDATE_SPEC));
            logger.debug(MSG_FAIL_DELETE_UPDATE_SPEC);
            return;
        }

        raise(new UpdaterInProgressEvent("Getting data from server", -1));
        Optional<VersionDescriptor> latestData = getLatestDataFromServer();

        // No data obtained from server
        if (!latestData.isPresent()) {
            raise(new UpdaterFailedEvent(MSG_NO_LATEST_DATA));
            logger.debug(MSG_NO_LATEST_DATA);
            return;
        }

        Optional<Version> latestVersion = getLatestVersion(latestData.get());

        // No latest version found
        if (!latestVersion.isPresent()) {
            raise(new UpdaterFailedEvent(MSG_NO_VERSION_AVAILABLE));
            logger.fatal(MSG_NO_VERSION_AVAILABLE);
            return;
        }

        // Wrong channel - EA <-> stable
        if (isOnSameUpdateChannel(latestVersion.get())) {
            raise(new UpdaterFailedEvent(MSG_DIFF_CHANNEL));
            logger.fatal(MSG_DIFF_CHANNEL);
            return;
        }

        // No updates
        if (downloadedVersions.contains(latestVersion.get()) || currentVersion.compareTo(latestVersion.get()) >= 0) {
            raise(new UpdaterFinishedEvent(MSG_NO_NEWER_VERSION));
            logger.debug(MSG_NO_NEWER_VERSION);
            return;
        }

        raise(new UpdaterInProgressEvent("Collecting all update files to be downloaded", -1));
        HashMap<String, URL> filesToBeUpdated;
        try {
            filesToBeUpdated = collectAllUpdateFilesToBeDownloaded(latestData.get());
        } catch (MalformedURLException e) {
            raise(new UpdaterFailedEvent(MSG_FAIL_MAIN_APP_URL));
            logger.debug(MSG_FAIL_MAIN_APP_URL);
            return;
        }

        if (filesToBeUpdated.isEmpty()) {
            raise(new UpdaterFinishedEvent(MSG_NO_UPDATE));
            logger.debug(MSG_NO_UPDATE);
            return;
        }

        try {
            downloadAllFilesToBeUpdated(new File(UPDATE_DIR), filesToBeUpdated);
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
            logger.debug(MSG_FAIL_CREATE_UPDATE_SPEC);
            return;
        }
        
        if (!isJarUpdaterResourceExist()) {
            raise(new UpdaterFailedEvent(MSG_JAR_UPDATER_MISSING));
            logger.fatal(MSG_JAR_UPDATER_MISSING);
            return;
        }
        
        try {
            extractJarUpdater();
        } catch (IOException e) {
            raise(new UpdaterFailedEvent(MSG_FAIL_EXTRACT_JAR_UPDATER));
            logger.debug(MSG_FAIL_EXTRACT_JAR_UPDATER);
            return;
        }

        raise(new UpdaterFinishedEvent("Update will be applied on next launch"));
        this.isUpdateApplicable = true;

        downloadedVersions.add(latestVersion.get());
    }

    private boolean isJarUpdaterResourceExist() {
        return new File(JAR_UPDATER_RESOURCE_PATH).exists();
    }

    /**
     * Get latest data from the server, containing version information as well as the required libraries
     */
    private Optional<VersionDescriptor> getLatestDataFromServer() {
        URL latestDataFileUrl;

        try {
            if (currentVersion.isEarlyAccess()) {
                latestDataFileUrl = new URL(VERSION_DESCRIPTOR_ON_SERVER_EARLY);
            } else {
                latestDataFileUrl = new URL(VERSION_DESCRIPTOR_ON_SERVER_STABLE);
            }
        } catch (MalformedURLException e) {
            logger.debug("Latest data file URL is invalid", e);
            return Optional.empty();
        }

        try {
            downloadFile(VERSION_DESCRIPTOR_FILE, latestDataFileUrl);
        } catch (IOException e) {
            logger.debug("Failed to download latest data");
            return Optional.empty();
        }

        try {
            return Optional.of(
                    StorageManager.deserializeObjectFromJsonFile(VERSION_DESCRIPTOR_FILE, VersionDescriptor.class));
        } catch (IOException e) {
            logger.debug("Failed to parse data from latest data file.", e);
        }

        return Optional.empty();
    }

    private Optional<Version> getLatestVersion(VersionDescriptor versionDescriptor) {
        try {
            return Optional.of(Version.fromString(versionDescriptor.getVersion()));
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to read latest version", e);
        }

        return Optional.empty();
    }

    private boolean isOnSameUpdateChannel(Version version) {
        return version.isEarlyAccess() != currentVersion.isEarlyAccess();
    }

    private HashMap<String, URL> collectAllUpdateFilesToBeDownloaded(VersionDescriptor versionDescriptor)
            throws MalformedURLException {
        OsDetector.Os machineOs = OsDetector.getOs();

        if (machineOs == OsDetector.Os.UNKNOWN) {
            logger.debug("OS not supported for updating");
            return new HashMap<>();
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
    private void downloadAllFilesToBeUpdated(File updateDir, HashMap<String, URL> filesToBeUpdated) throws IOException {
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
            logger.debug("Failed to download update for {}: {}", targetFile.toString(), e);
            throw e;
        }
    }

    private void createUpdateSpecification(HashMap<String, URL> filesToBeUpdated) throws IOException {
        LocalUpdateSpecificationHelper.saveLocalUpdateSpecFile(
                filesToBeUpdated.keySet().stream().collect(Collectors.toList())
        );
    }

    private void extractJarUpdater() throws IOException {
        File jarUpdaterFile = new File(JAR_UPDATER_APP_PATH);

        if (!jarUpdaterFile.exists() && !jarUpdaterFile.createNewFile()) {
            throw new IOException("Failed to create Jar Updater empty file");
        }

        try (InputStream in = UpdateManager.class.getClassLoader().getResourceAsStream(JAR_UPDATER_RESOURCE_PATH)) {
            Files.copy(in, jarUpdaterFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.debug("Failed to extract jar updater");
            throw e;
        }
    }

    private void writeDownloadedVersionsToFile(List<Version> downloadedVersions, File file) {
        try {
            if (FileUtil.isFileExists(file.toString())) {
                FileUtil.createFile(file);
            }

            StorageManager.serializeObjectToJsonFile(file, downloadedVersions);
        } catch (JsonProcessingException e) {
            logger.debug("Failed to convert downloaded version to JSON");
            e.printStackTrace();
        } catch (IOException e) {
            logger.debug("Failed to write downloaded version to file", e);
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

        writeDownloadedVersionsToFile(downloadedVersions, DOWNLOADED_VERSIONS_FILE);

        String jarUpdaterAppPath = JAR_UPDATER_APP_PATH;
        String localUpdateSpecFilepath = LocalUpdateSpecificationHelper.getLocalUpdateSpecFilepath();
        String cmdArg = String.format("--update-specification=%s --source-dir=%s", localUpdateSpecFilepath, UPDATE_DIR);

        String command = String.format("java -jar %1$s %2$s", jarUpdaterAppPath, cmdArg);

        logger.info("Starting jar updater with command " + command);

        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            logger.debug("Failed to run JarUpdater", e);
        }
    }

    public void stop() {
        applyUpdate();
    }
}
