package address.updater;

import address.storage.StorageManager;
import address.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Manages the LocalUpdateSpecification file
 */
public class LocalUpdateSpecificationHelper {
    private static final String LOCAL_UPDATE_DATA_FILE = UpdateManager.UPDATE_DIR + File.separator +
                                                         "UpdateSpecification";

    public static String getLocalUpdateSpecFilepath() {
        return LOCAL_UPDATE_DATA_FILE;
    }

    public static void clearLocalUpdateSpecFile() throws IOException {
        FileUtil.deleteFileIfExists(LOCAL_UPDATE_DATA_FILE);
    }

    public static void saveLocalUpdateSpecFile(List<String> affectedFiles) throws IOException {
        LocalUpdateSpecification localUpdateSpecification = new LocalUpdateSpecification(affectedFiles);

        StorageManager.serializeObjectToJsonFile(new File(LOCAL_UPDATE_DATA_FILE), localUpdateSpecification);
    }

    /**
     * @return first item is destination folder, the rest are affected files
     */
    public static List<String> readLocalUpdateSpecFile(String filepath) throws IOException {
        return StorageManager.deserializeObjectFromJsonFile(new File(filepath), LocalUpdateSpecification.class)
                .getLocalFilesToBeUpdated();
    }
}
