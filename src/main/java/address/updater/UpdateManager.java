package address.updater;

import address.controller.MainController;
import address.updater.model.FileUpdateDescriptor;
import address.updater.model.UpdateData;
import address.updater.model.VersionDescriptor;
import address.util.XmlFileHelper;
import address.util.FileUtil;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

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
import java.util.concurrent.TimeUnit;
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
        MainController.updaterStatusBar.displayMessage("Updating AddressBook...");
        DoubleProperty updateProgress = new SimpleDoubleProperty();
        MainController.updaterStatusBar.progressProperty().bind(updateProgress);
        updateProgress.set(0.1f);

        try {
            MainController.updaterStatusBar.displayMessage("Clearing local update specification file");
            simulateComputation();
            updateProgress.set(0.2f);
            LocalUpdateSpecificationHelper.clearLocalUpdateSpecFile();
        } catch (IOException e) {
            MainController.updaterStatusBar.displayMessage("Failed to delete previous update spec file");
            System.out.println("UpdateManager - Failed to delete previous update spec file");
            return;
        }

        MainController.updaterStatusBar.displayMessage("Getting update data from server");
        simulateComputation();
        updateProgress.set(0.3f);
        Optional<UpdateData> updateData = getUpdateDataFromServer();

        if (!updateData.isPresent()) {
            MainController.updaterStatusBar.displayMessage("There is no update data to be processed.");
            System.out.println("UpdateManager - There is no update data to be processed.");
            return;
        }

        MainController.updaterStatusBar.displayMessage("Collecting all downloaded update files");
        simulateComputation();
        updateProgress.set(0.4f);
        List<FileUpdateDescriptor> fileUpdateDescriptors = collectAllUpdateFilesToBeDownloaded(updateData.get());

        if (fileUpdateDescriptors.isEmpty()) {
            MainController.updaterStatusBar.displayMessage("There is no update");
            System.out.println("UpdateManager - There is no update.");
            return;
        }

        MainController.updaterStatusBar.displayMessage("Downloading all files that are to be updated");
        simulateComputation();
        updateProgress.set(0.6f);

        try {
            downloadAllFilesToBeUpdated(new File(UPDATE_DIRECTORY), fileUpdateDescriptors);
        } catch (IOException e) {
            MainController.updaterStatusBar.displayMessage("Downloading update failed");
            System.out.println("UpdateManager - Downloading update failed.");
            return;
        }

        MainController.updaterStatusBar.displayMessage("Creating update specification");
        simulateComputation();
        updateProgress.set(0.8f);

        try {
            createUpdateSpecification(fileUpdateDescriptors);
        } catch (IOException e) {
            MainController.updaterStatusBar.displayMessage("Failed to create update specification");
            System.out.println("UpdateManager - Failed to create update specification");
            return;
        }

        MainController.updaterStatusBar.displayMessage("Extracting Jar Updater");
        simulateComputation();
        updateProgress.set(0.9f);

        try {
            extractJarUpdater();
        } catch (IOException e) {
            MainController.updaterStatusBar.displayMessage("Failed to extract JAR updater");
            System.out.println("UpdateManager - Failed to extract JAR updater");
            return;
        }

        simulateComputation();
        updateProgress.set(1.0f);

        this.isUpdateApplicable = true;
    }

    private void simulateComputation() {
        try {
            TimeUnit.SECONDS.sleep(new Random().nextInt(5));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
