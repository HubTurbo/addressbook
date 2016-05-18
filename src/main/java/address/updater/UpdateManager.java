package address.updater;

import address.updater.model.FileUpdateDescriptor;
import address.updater.model.UpdateData;
import address.updater.model.VersionDescriptor;
import address.util.FileUtil;
import address.util.XmlHelper;

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

    private static final String JAR_UPDATER_APP_PATH = UPDATE_DIRECTORY + File.separator + "jarUpdater.jar";
    private static final int VERSION = 1;

    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    private boolean isUpdateApplicable;

    public UpdateManager() {
        this.isUpdateApplicable = false;
    }

    public void run() {
        pool.execute(() -> checkForUpdate());
    }

    private void checkForUpdate() {
        try {
            LocalUpdateSpecificationHelper.clearLocalUpdateSpecFile();
        } catch (IOException e) {
            System.out.println("UpdateManager - Failed to delete previous update spec file");
            return;
        }

        Optional<UpdateData> updateData = getUpdateDataFromServer();

        if (!updateData.isPresent()) {
            System.out.println("UpdateManager - There is no update data to be processed.");
            return;
        }

        List<FileUpdateDescriptor> fileUpdateDescriptors = collectAllUpdateFilesToBeDownloaded(updateData.get());

        if (fileUpdateDescriptors.isEmpty()) {
            System.out.println("UpdateManager - There is no update.");
            return;
        }

        try {
            downloadAllFilesToBeUpdated(new File(UPDATE_DIRECTORY), fileUpdateDescriptors);
        } catch (IOException e) {
            System.out.println("UpdateManager - Downloading update failed.");
            return;
        }

        try {
            createUpdateSpecification(fileUpdateDescriptors);
        } catch (IOException e) {
            System.out.println("UpdateManager - Failed to create update specification");
            return;
        }

        try {
            extractJarUpdater();
        } catch (IOException e) {
            System.out.println("UpdateManager - Failed to extract JAR updater");
            return;
        }

        this.isUpdateApplicable = true;
    }

    /**
     * (Dummy download) Get update data from a local file
     */
    private Optional<UpdateData> getUpdateDataFromServer() {
        File file = new File("update/UpdateData.xml");

        try {
            return Optional.of(XmlHelper.getUpdateDataFromFile(file));
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

        List<FileUpdateDescriptor> fileUpdateDescriptors = filesToBeDownloaded.entrySet().stream()
                .map(f -> new FileUpdateDescriptor(f.getKey(), f.getValue()))
                .collect(Collectors.toList());

        return fileUpdateDescriptors;
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
            if (!targetFile.exists()) {
                targetFile.mkdirs();
                targetFile.createNewFile();
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

        try (InputStream in = UpdateManager.class.getClassLoader().getResourceAsStream("updater/jarUpdater")) {
            Files.copy(in, jarUpdaterFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
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
