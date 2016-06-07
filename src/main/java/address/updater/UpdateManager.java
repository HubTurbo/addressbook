package address.updater;

import address.events.EventManager;
import address.events.UpdaterFinishedEvent;
import address.events.UpdaterInProgressEvent;
import address.updater.model.FileUpdateDescriptor;
import address.updater.model.UpdateData;
import address.updater.model.VersionDescriptor;
import address.util.JsonUtil;
import address.util.OsDetector;
import address.util.FileUtil;
import address.util.Version;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Checks for update to application
 */
public class UpdateManager {
    public static final String UPDATE_DIR = "update";

    // --- Messages
    private static final String MSG_FAIL_DELETE_UPDATE_SPEC = "Failed to delete previous update spec file";
    private static final String MSG_FAIL_DOWNLOAD_UPDATE = "Downloading update failed";
    private static final String MSG_FAIL_CREATE_UPDATE_SPEC = "Failed to create update specification";
    private static final String MSG_FAIL_EXTRACT_JAR_UPDATER = "Failed to extract JAR updater";
    private static final String MSG_NO_UPDATE_DATA = "There is no update data to be processed";
    private static final String MSG_NO_UPDATE = "There is no update";
    private static final String MSG_NO_VERSION_AVAILABLE = "No version to be downloaded";
    private static final String MSG_NO_NEWER_VERSION = "Version have been downloaded before; will not download again";
    // --- End of Messages

    private static final String JAR_UPDATER_RESOURCE_PATH = "updater/jarUpdater.jar";
    private static final String JAR_UPDATER_APP_PATH = UPDATE_DIR + File.separator + "jarUpdater.jar";
    private static final File DOWNLOADED_VERSIONS_FILE = new File(UPDATE_DIR + File.separator + "downloaded_versions");
    private static final String UPDATE_DATA_ON_SERVER =
            "https://raw.githubusercontent.com/HubTurbo/addressbook/master/UpdateData.json";
    private static final File UPDATE_DATA_FILE = new File(UPDATE_DIR + File.separator + "UpdateData.json");

    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final DependencyTracker dependencyTracker;
    private final BackupManager backupManager;
    private final List<Version> downloadedVersions;

    private boolean isUpdateApplicable;

    public UpdateManager() {
        this.isUpdateApplicable = false;
        dependencyTracker = new DependencyTracker();
        backupManager = new BackupManager(dependencyTracker);
        downloadedVersions = readDownloadedVersionsFromFile();
    }

    public List<String> getMissingDependencies() {
        return dependencyTracker.getMissingDependencies();
    }

    public void run() {
        pool.execute(backupManager::cleanupBackups);
        pool.execute(this::checkForUpdate);
    }

    private void checkForUpdate() {
        EventManager.getInstance().post(new UpdaterInProgressEvent("Clearing local update specification file", -1));
        try {
            LocalUpdateSpecificationHelper.clearLocalUpdateSpecFile();
        } catch (IOException e) {
            EventManager.getInstance().post(new UpdaterFinishedEvent(MSG_FAIL_DELETE_UPDATE_SPEC));
            System.out.println("UpdateManager - " + MSG_FAIL_DELETE_UPDATE_SPEC);
            return;
        }

        EventManager.getInstance().post(new UpdaterInProgressEvent("Getting data from server", -1));
        Optional<UpdateData> updateData = getUpdateDataFromServer();

        if (!updateData.isPresent()) {
            EventManager.getInstance().post(new UpdaterFinishedEvent(MSG_NO_UPDATE_DATA));
            System.out.println("UpdateManager - " + MSG_NO_UPDATE_DATA);
            return;
        }

        Optional<Version> latestVersion = getLatestVersion(updateData.get());

        if (!latestVersion.isPresent()) {
            EventManager.getInstance().post(new UpdaterFinishedEvent(MSG_NO_VERSION_AVAILABLE));
            System.out.println("UpdateManager - " + MSG_NO_VERSION_AVAILABLE);
            return;
        }

        if (downloadedVersions.contains(latestVersion.get())) {
            EventManager.getInstance().post(new UpdaterFinishedEvent(MSG_NO_NEWER_VERSION));
            System.out.println("UpdateManager - " + MSG_NO_NEWER_VERSION);
            return;
        }

        EventManager.getInstance().post(new UpdaterInProgressEvent(
                "Collecting all update files that are to be downloaded", -1));
        HashMap<String, URL> filesToBeUpdated = collectAllUpdateFilesToBeDownloaded(updateData.get());

        if (filesToBeUpdated.isEmpty()) {
            EventManager.getInstance().post(new UpdaterFinishedEvent(MSG_NO_UPDATE));
            System.out.println("UpdateManager - " + MSG_NO_UPDATE);
            return;
        }

        EventManager.getInstance().post(new UpdaterInProgressEvent("Downloading updates", 0.5));
        try {
            downloadAllFilesToBeUpdated(new File(UPDATE_DIR), filesToBeUpdated);
        } catch (IOException e) {
            EventManager.getInstance().post(new UpdaterFinishedEvent(MSG_FAIL_DOWNLOAD_UPDATE));
            System.out.println("UpdateManager - " + MSG_FAIL_DOWNLOAD_UPDATE);
            return;
        }

        EventManager.getInstance().post(new UpdaterInProgressEvent("Finalizing updates", 0.85));

        try {
            createUpdateSpecification(filesToBeUpdated);
        } catch (IOException e) {
            EventManager.getInstance().post(new UpdaterFinishedEvent(MSG_FAIL_CREATE_UPDATE_SPEC));
            System.out.println("UpdateManager - " + MSG_FAIL_CREATE_UPDATE_SPEC);
            return;
        }

        try {
            extractJarUpdater();
        } catch (IOException e) {
            EventManager.getInstance().post(new UpdaterFinishedEvent(MSG_FAIL_EXTRACT_JAR_UPDATER));
            System.out.println("UpdateManager - " + MSG_FAIL_EXTRACT_JAR_UPDATER);
            return;
        }

        EventManager.getInstance().post(new UpdaterFinishedEvent("Update will be applied on next launch"));
        this.isUpdateApplicable = true;

        updateDownloadedVersionsData(latestVersion.get());
    }

    /**
     * Get update data
     */
    private Optional<UpdateData> getUpdateDataFromServer() {
        try {
            FileUtil.createFile(UPDATE_DATA_FILE);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("UpdateManager - Failed to create update data file");
            return Optional.empty();
        }

        try {
            downloadFile(UPDATE_DATA_FILE, new URL(UPDATE_DATA_ON_SERVER));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println("UpdateManager - update data URL is invalid");
            return Optional.empty();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("UpdateManager - Failed to download update data");
            return Optional.empty();
        }

        try {
            return Optional.of(JsonUtil.fromJsonString(FileUtil.readFromFile(UPDATE_DATA_FILE), UpdateData.class));
        } catch (IOException e) {
            System.out.println("UpdateManager - Failed to parse update data from json file.");
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private Optional<Version> getLatestVersion(UpdateData updateData) {
        ArrayList<VersionDescriptor> versionFileChanges = updateData.getAllVersionFileChanges();

        return versionFileChanges.stream().map(VersionDescriptor::getVersion).max(Version::compareTo);
    }

    private HashMap<String, URL> collectAllUpdateFilesToBeDownloaded(UpdateData updateData) {
        FileUpdateDescriptor.Os machineOs;

        if (OsDetector.isOnWindows()) {
            machineOs = FileUpdateDescriptor.Os.WINDOWS;
        } else if (OsDetector.isOnMac()) {
            machineOs = FileUpdateDescriptor.Os.MAC;
        } else if (OsDetector.isOn32BitsLinux()) {
            machineOs = FileUpdateDescriptor.Os.LINUX32;
        } else if (OsDetector.isOn64BitsLinux()) {
            machineOs = FileUpdateDescriptor.Os.LINUX64;
        } else {
            System.out.println("UpdateManager - OS not supported for updating");
            return new HashMap<>();
        }

        ArrayList<VersionDescriptor> versionFileChanges = updateData.getAllVersionFileChanges();

        List<VersionDescriptor> relevantVersionFileChanges = versionFileChanges.stream()
                .filter(versionChangesDescriptor ->
                        versionChangesDescriptor.getVersion().compareTo(Version.getCurrentVersion()) > 0)
                .sorted().collect(Collectors.toList());

        HashMap<String, URL> filesToBeDownloaded = new HashMap<>();

        for (VersionDescriptor versionDescriptor : relevantVersionFileChanges) {
            versionDescriptor.getFileUpdateDescriptors().stream()
                    .filter(fileUpdateDescriptor -> fileUpdateDescriptor.getOs() == FileUpdateDescriptor.Os.ALL ||
                            fileUpdateDescriptor.getOs() == machineOs)
                    .forEach(fileUpdateDescriptor -> filesToBeDownloaded.put(fileUpdateDescriptor.getDestinationFile(),
                            fileUpdateDescriptor.getDownloadLink()));
        }

        return filesToBeDownloaded;
    }

    /**
     * @param updateDir directory to store downloaded updates
     */
    private void downloadAllFilesToBeUpdated(File updateDir, HashMap<String, URL> filesToBeUpdated)
            throws IOException {
        if (!updateDir.exists() || !updateDir.isDirectory()) {
            try {
                Files.createDirectory(updateDir.toPath());
            } catch (IOException e) {
                System.out.println("Failed to create update directory");
                e.printStackTrace();
            }
        }

        for (String destFile : filesToBeUpdated.keySet()) {
            try {
                downloadFile(new File(updateDir.toString(), destFile), filesToBeUpdated.get(destFile));
            } catch (IOException e) {
                System.out.println("Failed to download an update file, aborting update.");
                throw e;
            }
        }
    }

    private void downloadFile(File targetFile, URL source) throws IOException {
        try (InputStream in = source.openStream()) {
            if (!FileUtil.createFile(targetFile)) {
                System.out.println("File already exists");
            }
            Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println(String.format("UpdateManager - Failed to download update for %s",
                    targetFile.toString()));
            e.printStackTrace();
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
            System.out.println("UpdateManager - Failed to extract jar updater");
            throw e;
        }
    }

    private void updateDownloadedVersionsData(Version latestVersionDownloaded) {
        downloadedVersions.add(latestVersionDownloaded);
        writeDownloadedVersionsToFile();
    }

    private void writeDownloadedVersionsToFile() {
        try {
            if (FileUtil.isFileExists(DOWNLOADED_VERSIONS_FILE.toString())) {
                FileUtil.createFile(DOWNLOADED_VERSIONS_FILE);
            }

            FileUtil.writeToFile(DOWNLOADED_VERSIONS_FILE, JsonUtil.toJsonString(downloadedVersions));
        } catch (JsonProcessingException e) {
            System.out.println("Failed to convert downloaded version to JSON");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Failed to write downloaded version to file");
            e.printStackTrace();
        }
    }

    private List<Version> readDownloadedVersionsFromFile() {
        try {
            return JsonUtil.fromJsonStringToList(FileUtil.readFromFile(DOWNLOADED_VERSIONS_FILE), Version.class);
        } catch (IOException e) {
            System.out.println("Failed to read downloaded version from file");
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public void applyUpdate() {
        if (!this.isUpdateApplicable) {
            return;
        }

        if (!backupManager.createBackupOfCurrentApp()) {
            return;
        }

        String restarterAppPath = JAR_UPDATER_APP_PATH;
        String localUpdateSpecFilepath = System.getProperty("user.dir") + File.separator +
                LocalUpdateSpecificationHelper.getLocalUpdateSpecFilepath();
        String cmdArg = String.format("--update-specification=%s --source-dir=%s",
                localUpdateSpecFilepath, UPDATE_DIR);

        String command = String.format("java -jar %1$s %2$s", restarterAppPath, cmdArg);

        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
