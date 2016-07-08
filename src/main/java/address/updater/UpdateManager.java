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
 * Checks for update to application and download and apply it if it is available
 * For more details, see documentation
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
    private static final String MSG_NO_UPDATE_DATA = "There is no update data to be processed";
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
        downloadedVersions = readDownloadedVersionsFromFile();
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
        Optional<VersionDescriptor> updateData = getUpdateDataFromServer();

        if (!updateData.isPresent()) {
            raise(new UpdaterFailedEvent(MSG_NO_UPDATE_DATA));
            logger.debug(MSG_NO_UPDATE_DATA);
            return;
        }

        Optional<Version> latestVersion = getLatestVersion(updateData.get());

        if (!latestVersion.isPresent()) {
            raise(new UpdaterFailedEvent(MSG_NO_VERSION_AVAILABLE));
            logger.fatal(MSG_NO_VERSION_AVAILABLE);
            return;
        }

        if (isOnSameUpdateChannel(latestVersion.get())) {
            raise(new UpdaterFailedEvent(MSG_DIFF_CHANNEL));
            logger.fatal(MSG_DIFF_CHANNEL);
            return;
        }

        if (downloadedVersions.contains(latestVersion.get()) || currentVersion.compareTo(latestVersion.get()) >= 0) {
            raise(new UpdaterFinishedEvent(MSG_NO_NEWER_VERSION));
            logger.debug(MSG_NO_NEWER_VERSION);
            return;
        }

        raise(new UpdaterInProgressEvent("Collecting all update files that are to be downloaded", -1));
        HashMap<String, URL> filesToBeUpdated;
        try {
            filesToBeUpdated = collectAllUpdateFilesToBeDownloaded(updateData.get());
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
        
        if (isJarUpdaterExist()) {
            raise(new UpdaterFailedEvent(MSG_JAR_UPDATER_MISSING));
            logger.debug(MSG_JAR_UPDATER_MISSING);
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

    private boolean isJarUpdaterExist() {
        return !new File(JAR_UPDATER_RESOURCE_PATH).exists();
    }

    /**
     * Get update data
     */
    private Optional<VersionDescriptor> getUpdateDataFromServer() {
        URL updateDataUrl;

        try {
            if (currentVersion.isEarlyAccess()) {
                updateDataUrl = new URL(VERSION_DESCRIPTOR_ON_SERVER_EARLY);
            } else {
                updateDataUrl = new URL(VERSION_DESCRIPTOR_ON_SERVER_STABLE);
            }
        } catch (MalformedURLException e) {
            logger.debug("Update data URL is invalid", e);
            return Optional.empty();
        }

        try {
            downloadFile(VERSION_DESCRIPTOR_FILE, updateDataUrl);
        } catch (IOException e) {
            logger.debug("Failed to download update data");
            return Optional.empty();
        }

        try {
            return Optional.of(
                    StorageManager.deserializeObjectFromJsonFile(VERSION_DESCRIPTOR_FILE, VersionDescriptor.class));
        } catch (IOException e) {
            logger.debug("Failed to parse update data from json file.", e);
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

        URL mainAppDownloadLink;

        mainAppDownloadLink = versionDescriptor.getDownloadLinkForMainApp();

        filesToBeDownloaded.put("addressbook.jar", mainAppDownloadLink);

        versionDescriptor.getLibraries().stream()
                .filter(libDesc -> libDesc.getOs() == OsDetector.Os.ANY || libDesc.getOs() == OsDetector.getOs())
                .filter(libDesc -> !FileUtil.isFileExists("lib/" + libDesc.getFilename()))
                .forEach(libDesc -> filesToBeDownloaded.put("lib/" + libDesc.getFilename(), libDesc.getDownloadLink()));

        return filesToBeDownloaded;
    }

    /**
     * @param updateDir directory to store downloaded updates
     */
    private void downloadAllFilesToBeUpdated(File updateDir, HashMap<String, URL> filesToBeUpdated) throws IOException {
        if (!FileUtil.isDirExists(updateDir)) {
            try {
                Files.createDirectory(updateDir.toPath());
            } catch (IOException e) {
                logger.debug("Failed to create update directory", e);
            }
        }

        int noOfFilesTobeDownloaded = filesToBeUpdated.keySet().size();
        int noOfFilesDownloaded = 0;

        for (String destFile : filesToBeUpdated.keySet()) {
            try {
                downloadFile(new File(updateDir.toString(), destFile), filesToBeUpdated.get(destFile));
            } catch (IOException e) {
                logger.debug("Failed to download an update file, aborting update.");
                throw e;
            }

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
            logger.debug("Failed to download update for {}", targetFile.toString(), e);
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

    private void writeDownloadedVersionsToFile() {
        try {
            if (FileUtil.isFileExists(DOWNLOADED_VERSIONS_FILE.toString())) {
                FileUtil.createFile(DOWNLOADED_VERSIONS_FILE);
            }

            StorageManager.serializeObjectToJsonFile(DOWNLOADED_VERSIONS_FILE, downloadedVersions);
        } catch (JsonProcessingException e) {
            logger.debug("Failed to convert downloaded version to JSON");
            e.printStackTrace();
        } catch (IOException e) {
            logger.debug("Failed to write downloaded version to file", e);
        }
    }

    private List<Version> readDownloadedVersionsFromFile() {
        try {
            return JsonUtil.fromJsonStringToList(FileUtil.readFromFile(DOWNLOADED_VERSIONS_FILE), Version.class);
        } catch (IOException e) {
            logger.warn("Failed to read downloaded version from file: {}", e);
        }

        return new ArrayList<>();
    }

    public void applyUpdate() {
        if (!ManifestFileReader.isRunFromJar()) {
            return;
        }

        if (!this.isUpdateApplicable) {
            return;
        }

        try {
            backupHandler.createBackupOfApp(currentVersion);
        } catch (IOException | URISyntaxException e) {
            logger.fatal("Failed to create backup of app; not applying update");
            return;
        }

        writeDownloadedVersionsToFile();

        String restarterAppPath = JAR_UPDATER_APP_PATH;
        String localUpdateSpecFilepath = LocalUpdateSpecificationHelper.getLocalUpdateSpecFilepath();
        String cmdArg = String.format("--update-specification=%s --source-dir=%s", localUpdateSpecFilepath, UPDATE_DIR);

        String command = String.format("java -jar %1$s %2$s", restarterAppPath, cmdArg);

        logger.info("Restarting with command " + command);

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
