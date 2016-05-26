package address.updater;

import address.events.EventManager;
import address.events.UpdaterCompletedEvent;
import address.events.UpdaterInProgressEvent;
import address.updater.model.FileUpdateDescriptor;
import address.updater.model.UpdateData;
import address.updater.model.VersionDescriptor;
import address.util.XmlFileHelper;
import address.util.FileUtil;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Checks for update to application
 */
public class UpdateManager {
    public static final String UPDATE_DIRECTORY = "update";

    private static final String JAR_UPDATER_RESOURCE_PATH = "updater/jarUpdater.jar";
    private static final String JAR_UPDATER_APP_PATH = UPDATE_DIRECTORY + File.separator + "jarUpdater.jar";
    private static final int VERSION = 1;

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
            EventManager.getInstance().post(new UpdaterInProgressEvent(
                                            "UpdateManager - Failed to delete previous update spec file", 0.0));
            System.out.println("UpdateManager - Failed to delete previous update spec file");
            return;
        }

        EventManager.getInstance().post(new UpdaterInProgressEvent("Getting data from server", -1));
        Optional<UpdateData> updateData = getUpdateDataFromServer();

        if (!updateData.isPresent()) {
            EventManager.getInstance().post(new UpdaterInProgressEvent(
                    "UpdateManager - There is no update data to be processed.", 0.0));
            EventManager.getInstance().post(new UpdaterCompletedEvent());
            System.out.println("UpdateManager - There is no update data to be processed.");
            return;
        }

        EventManager.getInstance().post(new UpdaterInProgressEvent(
                                        "Collecting all update files that are to be downloaded", -1));
        List<FileUpdateDescriptor> fileUpdateDescriptors = collectAllUpdateFilesToBeDownloaded(updateData.get());

        if (fileUpdateDescriptors.isEmpty()) {
            EventManager.getInstance().post(new UpdaterInProgressEvent(
                    "UpdateManager - There is no update", 0.0));
            System.out.println("UpdateManager - There is no update.");
            EventManager.getInstance().post(new UpdaterCompletedEvent());
            return;
        }

        EventManager.getInstance().post(new UpdaterInProgressEvent("Downloading all files that are to be updated",
                                                                   0.5));
        try {
            downloadAllFilesToBeUpdated(new File(UPDATE_DIRECTORY), fileUpdateDescriptors);
        } catch (IOException e) {
            EventManager.getInstance().post(new UpdaterInProgressEvent(
                    "UpdateManager - Downloading update failed", 0.0));
            System.out.println("UpdateManager - Downloading update failed.");
            EventManager.getInstance().post(new UpdaterCompletedEvent());
            return;
        }
        EventManager.getInstance().post(new UpdaterInProgressEvent("Downloaded all files that are to be updated", 0.8));
        EventManager.getInstance().post(new UpdaterInProgressEvent("Creating update specification", -1));

        try {
            createUpdateSpecification(fileUpdateDescriptors);
        } catch (IOException e) {
            EventManager.getInstance().post(new UpdaterInProgressEvent(
                    "UpdateManager - Failed to create update specification", 0.0));
            System.out.println("UpdateManager - Failed to create update specification");
            EventManager.getInstance().post(new UpdaterCompletedEvent());
            return;
        }

        EventManager.getInstance().post(new UpdaterInProgressEvent("Extracting Jar Updater", -1));

        try {
            extractJarUpdater();
        } catch (IOException e) {
            EventManager.getInstance().post(new UpdaterInProgressEvent(
                    "UpdateManager - Failed to extract JAR updater", 0.0));
            System.out.println("UpdateManager - Failed to extract JAR updater");
            EventManager.getInstance().post(new UpdaterCompletedEvent());
            return;
        }
        EventManager.getInstance().post(new UpdaterCompletedEvent());
        this.isUpdateApplicable = true;
    }

    /**
     * (Dummy download) Get update data from a local file
     */
    private Optional<UpdateData> getUpdateDataFromServer() {
        File file = new File("update/UpdateData.xml");

        try {
            return Optional.of(XmlFileHelper.getUpdateDataFromFile(file));
        } catch (JAXBException e) {
            System.out.println("UpdateManager - Failed to parse update data from xml file.");
        }

        return Optional.empty();
    }

    private List<FileUpdateDescriptor> collectAllUpdateFilesToBeDownloaded(UpdateData updateData) {
        ArrayList<VersionDescriptor> versionFileChanges = updateData.getAllVersionFileChanges();

        List<VersionDescriptor> relevantVersionFileChanges = versionFileChanges.stream()
                .filter(versionChangesDescriptor -> versionChangesDescriptor.getVersionNumber() > VERSION)
                .sorted().collect(Collectors.toList());

        HashMap<URI, URL> filesToBeDownloaded = new HashMap<>();

        for (VersionDescriptor versionDescriptor : relevantVersionFileChanges) {
            for (FileUpdateDescriptor fileUpdateDescriptor : versionDescriptor.getFileUpdateDescriptors()) {
                filesToBeDownloaded.put(fileUpdateDescriptor.getFilePath(), fileUpdateDescriptor.getDownloadLink());
            }
        }

        return filesToBeDownloaded.entrySet().stream().map(f -> new FileUpdateDescriptor(f.getKey(), f.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * @param updateDir directory to store downloaded updates
     */
    private void downloadAllFilesToBeUpdated(File updateDir, List<FileUpdateDescriptor> fileUpdateDescriptors)
            throws IOException {
        if (!updateDir.exists() || !updateDir.isDirectory()) {
            try {
                Files.createDirectory(updateDir.toPath());
            } catch (IOException e) {
                System.out.println("Failed to create update directory");
                e.printStackTrace();
            }
        }

        for (FileUpdateDescriptor fileUpdateDescriptor : fileUpdateDescriptors) {
            try {
                downloadFile(Paths.get(updateDir.toString(), fileUpdateDescriptor.getFilePath().toString()).toFile(),
                        fileUpdateDescriptor.getDownloadLink());
            } catch (IOException e) {
                System.out.println("Failed to download an update file, aborting update.");
                throw e;
            }
        }
    }

    private void downloadFile(File targetFile, URL source) throws IOException {
        try (InputStream in = source.openStream()) {
            if (!FileUtil.createFile(targetFile)) {
                throw new IOException("Error creating new file.");
            }
            Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println(String.format("UpdateManager - Failed to download update for %s",
                                             targetFile.toString()));
            throw e;
        }
    }

    private void createUpdateSpecification(List<FileUpdateDescriptor> fileUpdateDescriptors) throws IOException {
        LocalUpdateSpecificationHelper.saveLocalUpdateSpecFile(
                fileUpdateDescriptors.stream()
                        .map(f -> Paths.get(f.getFilePath().toString()).toString())
                        .collect(Collectors.toList()));
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
}
