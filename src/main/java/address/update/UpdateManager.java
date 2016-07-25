package address.update;

import address.events.update.ApplicationUpdateFailedEvent;
import address.events.update.ApplicationUpdateFinishedEvent;
import address.events.update.ApplicationUpdateInProgressEvent;
import address.main.ComponentManager;
import address.util.AppLogger;
import address.util.LoggerManager;
import address.util.ManifestFileReader;
import commons.*;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
 * <p>
 * Data required for the update will be downloaded in the background while the app is running, and a local specification
 * file will be produced for another component to read and do the update migration.
 */
public class UpdateManager extends ComponentManager {
    private static final String UPDATE_DIR = "update";
    private static final AppLogger logger = LoggerManager.getLogger(UpdateManager.class);
    // --- Messages
    private static final String MSG_FAIL_DELETE_UPDATE_SPEC = "Failed to delete previous update spec file";
    private static final String MSG_FAIL_DOWNLOAD_UPDATE = "Downloading update failed";
    private static final String MSG_FAIL_CREATE_UPDATE_SPEC = "Failed to create update specification";
    private static final String MSG_FAIL_UPDATE_NOT_SUPPORTED = "Update not supported on detected OS";
    private static final String MSG_FAIL_CREATE_UPDATE_DIRECTORY = "Error creating update directory";
    private static final String MSG_FAIL_OBTAIN_LATEST_VERSION_DATA = "Unable to obtain latest version data. Please manually download the latest version.";
    private static final String MSG_FAIL_READ_LATEST_VERSION = "Error reading latest version";
    private static final String MSG_FAIL_UPDATE_BACKUP_VERSIONS_DATA = "Error updating backup versions' data file";
    private static final String MSG_IN_PROGRESS_FINALIZING_UPDATES = "Finalizing updates";
    private static final String MSG_IN_PROGRESS_COLLECTING_UPDATE_FILES = "Collecting all update files to be downloaded";
    private static final String MSG_IN_PROGRESS_DOWNLOADING_LATEST_VERSION_DATA = "Downloading latest version data from server";
    private static final String MSG_IN_PROGRESS_READING_LATEST_SERVER_DATA = "Reading downloaded latest version data";
    private static final String MSG_IN_PROGRESS_READING_LATEST_VERSION = "Reading latest version";
    private static final String MSG_IN_PROGRESS_CHECKING_IF_UPDATE_REQUIRED = "Checking if update is required";
    private static final String MSG_IN_PROGRESS_DELETING_PREVIOUS_SPECIFICATION_FILE = "Clearing local update specification file";
    private static final String MSG_FINISHED_DEVELOPER_ENV = "Developer env detected; not updating";
    private static final String MSG_FINISHED_UP_TO_DATE = "Up-to-date";
    private static final String MSG_FINISHED_UPDATE = "Update will be applied on next launch";
    // --- End of Messages
    private static final String VERSION_DATA_ON_SERVER_STABLE =
            "https://raw.githubusercontent.com/HubTurbo/addressbook/stable/VersionData.json";
    private static final String VERSION_DATA_ON_SERVER_EARLY_ACCESS =
            "https://raw.githubusercontent.com/HubTurbo/addressbook/early-access/VersionData.json";
    private static final File DOWNLOADED_VERSIONS_FILE = new File(UPDATE_DIR + File.separator + "downloaded_versions");
    private static final File VERSION_DESCRIPTOR_FILE = new File(UPDATE_DIR + File.separator + "VersionData.json");
    private static final String LIB_DIR = "lib/";
    private static final String LAUNCHER_FILE_REGEX = "addressbook-V\\d\\.\\d\\.\\d(ea)?\\.jar";
    private static final String UPDATER_FILE_PATH = "update/updater.jar";

    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final Version currentVersion;
    private final List<Version> downloadedVersions;

    private boolean shouldRunUpdater;

    public UpdateManager(Version currentVersion) {
        super();
        this.currentVersion = currentVersion;
        downloadedVersions = getDownloadedVersionsFromFile(DOWNLOADED_VERSIONS_FILE);
        shouldRunUpdater = false;
    }

    public void start() {
        if (!ManifestFileReader.isRunFromJar()) {
            raise(new ApplicationUpdateFinishedEvent(MSG_FINISHED_DEVELOPER_ENV));
            return;
        }
        pool.execute(this::checkForUpdate);
    }

    public void stop() {
        if (!shouldRunUpdater) {
            logger.info("Not running updater.");
            return;
        }
        startUpdater();
    }

    private void startUpdater() {
        logger.info("Scheduling updater");
        try {
            extractUpdaterJar();
            runUpdater();
        } catch (IOException e) {
            logger.fatal("Error scheduling updater: {}", e);
        }
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

    private InputStream getResourceStream(String resourcePath) {
        return UpdateManager.class.getClassLoader().getResourceAsStream(resourcePath);
    }

    private void extractUpdaterJar() throws IOException {
        logger.info("Extracting updater jar to: " + UPDATER_FILE_PATH);
        extractFile(new File(UPDATER_FILE_PATH), "updater/updater.jar");
    }

    private void runUpdater() throws IOException {
        try {
            String command = "java -cp " + UPDATER_FILE_PATH + " updater.UpdateMigrator";
            logger.debug("Starting updater: {}", command);
            Runtime.getRuntime().exec(command, null, new File(System.getProperty("user.dir")));
            logger.debug("Updater launched");
        } catch (IOException e) {
            throw new IOException("Error launching updater", e);
        }
    }

    /**
     * Read latest data containing version information as well as the required libraries
     */
    private VersionData readLatestVersionData(File latestVersionDataFile) throws IOException {
        return FileUtil.deserializeObjectFromJsonFile(latestVersionDataFile, VersionData.class);
    }

    /**
     * Downloads the latest version data from the server into latestVersionDataFile
     * @param latestVersionDataFile
     * @throws IOException
     */
    private void downloadLatestVersionDataFromServer(File latestVersionDataFile) throws IOException {
        URL latestDataFileUrl = getLatestVersionDataUrl(currentVersion.isEarlyAccess());
        downloadFile(latestVersionDataFile, latestDataFileUrl);
    }

    private void checkForUpdate() {
        raise(new ApplicationUpdateInProgressEvent(MSG_IN_PROGRESS_DELETING_PREVIOUS_SPECIFICATION_FILE, -1));
        try {
            LocalUpdateSpecificationHelper.clearLocalUpdateSpecFile();
        } catch (IOException e) {
            raise(new ApplicationUpdateFailedEvent(MSG_FAIL_DELETE_UPDATE_SPEC));
            return;
        }

        raise(new ApplicationUpdateInProgressEvent(MSG_IN_PROGRESS_DOWNLOADING_LATEST_VERSION_DATA, -1));
        try {
            downloadLatestVersionDataFromServer(VERSION_DESCRIPTOR_FILE);
        } catch (IOException e) {
            raise(new ApplicationUpdateFailedEvent(MSG_FAIL_OBTAIN_LATEST_VERSION_DATA));
            return;
        }

        raise(new ApplicationUpdateInProgressEvent(MSG_IN_PROGRESS_READING_LATEST_SERVER_DATA, -1));
        VersionData latestData;
        try {
            latestData = readLatestVersionData(VERSION_DESCRIPTOR_FILE);
        } catch (IOException e) {
            raise(new ApplicationUpdateFailedEvent(MSG_FAIL_OBTAIN_LATEST_VERSION_DATA));
            return;
        }

        raise(new ApplicationUpdateInProgressEvent(MSG_IN_PROGRESS_READING_LATEST_VERSION, -1));
        Version latestVersion;
        try {
            latestVersion = getVersion(latestData);
        } catch (IllegalArgumentException e) {
            raise(new ApplicationUpdateFailedEvent(MSG_FAIL_READ_LATEST_VERSION));
            return;
        }

        // Close app if wrong release channel - EA <-> stable
        assert isOnSameReleaseChannel(latestVersion) : "Error: latest version found to be in the wrong release channel";

        raise(new ApplicationUpdateInProgressEvent(MSG_IN_PROGRESS_CHECKING_IF_UPDATE_REQUIRED, -1));
        if (currentVersion.compareTo(latestVersion) >= 0) {
            raise(new ApplicationUpdateFinishedEvent(MSG_FINISHED_UP_TO_DATE));
            return;
        }

        raise(new ApplicationUpdateInProgressEvent(MSG_IN_PROGRESS_COLLECTING_UPDATE_FILES, -1));
        HashMap<String, URL> filesToBeUpdated;
        try {
            filesToBeUpdated = getFilesToDownload(latestData);
        } catch (UnsupportedOperationException e) {
            raise(new ApplicationUpdateFailedEvent(MSG_FAIL_UPDATE_NOT_SUPPORTED));
            return;
        }

        assert !filesToBeUpdated.isEmpty() : "No files to be updated"; // at least launcher and main app must be updated

        try {
            createUpdateDir();
        } catch (IOException e) {
            raise(new ApplicationUpdateFailedEvent(MSG_FAIL_CREATE_UPDATE_DIRECTORY));
        }

        try {
            downloadFilesToBeUpdated(new File(UPDATE_DIR), filesToBeUpdated);
        } catch (IOException e) {
            raise(new ApplicationUpdateFailedEvent(MSG_FAIL_DOWNLOAD_UPDATE));
            return;
        }

        raise(new ApplicationUpdateInProgressEvent(MSG_IN_PROGRESS_FINALIZING_UPDATES, -1));

        try {
            createUpdateSpecification(filesToBeUpdated);
        } catch (IOException e) {
            raise(new ApplicationUpdateFailedEvent(MSG_FAIL_CREATE_UPDATE_SPEC));
            return;
        }

        downloadedVersions.add(latestVersion);
        try {
            writeDownloadedVersionsToFile(downloadedVersions, DOWNLOADED_VERSIONS_FILE);
        } catch (IOException e) {
            raise(new ApplicationUpdateFailedEvent(MSG_FAIL_UPDATE_BACKUP_VERSIONS_DATA));
            return;
        }

        raise(new ApplicationUpdateFinishedEvent(MSG_FINISHED_UPDATE));
        shouldRunUpdater = true;
    }

    private void createUpdateDir() throws IOException {
        File updateDir = new File(UPDATE_DIR);
        if (!FileUtil.isDirExists(updateDir)) {
            Files.createDirectory(updateDir.toPath());
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
        return Version.fromString(versionData.getVersion());
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
        if (commons.OsDetector.getOs() == commons.OsDetector.Os.UNKNOWN) {
            throw new UnsupportedOperationException("OS not supported for updating");
        }

        List<LibraryDescriptor> librariesForOs = getLibrariesForOs(versionData.getLibraries(), commons.OsDetector.getOs());
        List<LibraryDescriptor> librariesToDownload = getLibrariesToDownload(librariesForOs);

        return getLibraryFilesDownloadLinks(librariesToDownload);
    }

    private List<LibraryDescriptor> getLibrariesToDownload(List<LibraryDescriptor> requiredLibraryFiles) {
        return requiredLibraryFiles.stream()
                .filter(libDesc -> !FileUtil.isFileExists(LIB_DIR + libDesc.getFileName()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Converts a list of library files into a map of file paths and their respective download urls
     *
     * @param libraryFiles
     * @return
     */
    private HashMap<String, URL> getLibraryFilesDownloadLinks(List<LibraryDescriptor> libraryFiles) {
        HashMap<String, URL> filesToBeDownloaded = new HashMap<>();
        libraryFiles.forEach(libDesc -> {
            String filePath = getFilePath(libDesc.getFileName());
            filesToBeDownloaded.put(filePath, libDesc.getDownloadLink());
        });
        return filesToBeDownloaded;
    }

    private String getFilePath(String fileName) {
        if (fileName.matches(LAUNCHER_FILE_REGEX)) {
            return fileName;
        } else {
            return LIB_DIR + fileName;
        }
    }

    /**
     * Returns the list of libraries which are either universal or match the given os
     *
     * @param libraries
     * @param Os
     * @return
     */
    private List<LibraryDescriptor> getLibrariesForOs(List<LibraryDescriptor> libraries, commons.OsDetector.Os Os) {
        return libraries.stream()
                .filter(libDesc -> libDesc.getOs() == commons.OsDetector.Os.ANY || libDesc.getOs() == Os)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * @param updateDir        directory to store downloaded updates
     * @param filesToBeUpdated files to download for update
     */
    private void downloadFilesToBeUpdated(File updateDir, HashMap<String, URL> filesToBeUpdated) throws IOException {
        int totalFilesToDownload = filesToBeUpdated.keySet().size();
        int noOfFilesDownloaded = 0;

        for (String destFile : filesToBeUpdated.keySet()) {
            downloadFile(new File(updateDir.toString(), destFile), filesToBeUpdated.get(destFile));
            noOfFilesDownloaded++;
            double progress = (1.0 * noOfFilesDownloaded) / totalFilesToDownload;
            raise(new ApplicationUpdateInProgressEvent("Downloading updates", progress));
        }
    }

    /**
     * Downloads a file from source url into targetFile
     * <p>
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
     * Writes a stream content into a target file
     * <p>
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

    private InputStream getUrlStream(URL source) throws IOException {
        return source.openStream();
    }

    private void createFile(File targetFile) throws IOException {
        FileUtil.createFile(targetFile);
    }

    private void createUpdateSpecification(HashMap<String, URL> filesToBeUpdated) throws IOException {
        List<String> listOfFiles = filesToBeUpdated.keySet().stream().collect(Collectors.toList());
        listOfFiles.add("VersionData.json");
        LocalUpdateSpecificationHelper.saveLocalUpdateSpecFile(listOfFiles);
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
            logger.debug("Failed to read downloaded version from file: {}", e);
            return new ArrayList<>();
        }
    }
}
