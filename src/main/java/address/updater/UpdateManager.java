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

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Checks for update to application
 */
public class UpdateManager {
    public static final String UPDATE_DIRECTORY = "update";

    // --- Messages
    private static final String MSG_FAIL_DELETE_UPDATE_SPEC = "Failed to delete previous update spec file";
    private static final String MSG_FAIL_DOWNLOAD_UPDATE = "Downloading update failed";
    private static final String MSG_FAIL_CREATE_UPDATE_SPEC = "Failed to create update specification";
    private static final String MSG_FAIL_EXTRACT_JAR_UPDATER = "Failed to extract JAR updater";
    private static final String MSG_NO_UPDATE_DATA = "There is no update data to be processed";
    private static final String MSG_NO_UPDATE = "There is no update";
    // --- End of Messages

    private static final String JAR_UPDATER_RESOURCE_PATH = "updater/jarUpdater.jar";
    private static final String JAR_UPDATER_APP_PATH = UPDATE_DIRECTORY + File.separator + "jarUpdater.jar";
    private static final int VERSION = 0;
    private static final String BACKUP_SUFFIX = "_V";

    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    private boolean isUpdateApplicable;

    public UpdateManager() {
        this.isUpdateApplicable = false;
    }

    public void run() {
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
            downloadAllFilesToBeUpdated(new File(UPDATE_DIRECTORY), filesToBeUpdated);
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
    }

    /**
     * (Dummy download) Get update data from a local file
     */
    private Optional<UpdateData> getUpdateDataFromServer() {
        File file = new File("update/UpdateData.json");

        try {
            return Optional.of(JsonUtil.fromJsonString(FileUtil.readFromFile(file), UpdateData.class));
        } catch (IOException e) {
            System.out.println("UpdateManager - Failed to parse update data from json file.");
            e.printStackTrace();
        }

        return Optional.empty();
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
                .filter(versionChangesDescriptor -> versionChangesDescriptor.getVersionNumber() > VERSION)
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

    public void applyUpdate() {
        if (!this.isUpdateApplicable) {
            return;
        }

        if (!backupMainApp()) {
            return;
        }

        String restarterAppPath = JAR_UPDATER_APP_PATH;
        String localUpdateSpecFilepath = System.getProperty("user.dir") + File.separator +
                                         LocalUpdateSpecificationHelper.getLocalUpdateSpecFilepath();
        String cmdArg = String.format("--update-specification=%s --source-dir=%s",
                                      localUpdateSpecFilepath, UPDATE_DIRECTORY);

        String command = String.format("java -jar %1$s %2$s", restarterAppPath, cmdArg);

        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return true if backup is successfully made or if app is run from backup jar hence no backup need to be made
     */
    private boolean backupMainApp() {
        File mainAppJar = FileUtil.getJarFileOfClass(this.getClass());

        if (isRunFromBackupJar(mainAppJar.getName())) {
            System.out.println("Run from a backup; not creating backup");
            return true;
        }

        String backupFilename = getBackupFilename(mainAppJar.getName());

        try {
            FileUtil.copyFile(mainAppJar.toPath(), Paths.get(backupFilename), true);
        } catch (IOException e) {
            System.out.println("Failed to create backup");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean isRunFromBackupJar(String jarName) {
        return jarName.contains(BACKUP_SUFFIX);
    }

    private String getBackupFilename(String jarName) {
        Pattern jarFilenamePattern = Pattern.compile("^(.*)\\.jar$", Pattern.CASE_INSENSITIVE);
        Matcher jarFilenameMatcher = jarFilenamePattern.matcher(jarName);

        if (!jarFilenameMatcher.find()) {
            return jarName + BACKUP_SUFFIX + VERSION;
        }

        return jarFilenameMatcher.group(1) + BACKUP_SUFFIX + VERSION + ".jar";
    }
}
