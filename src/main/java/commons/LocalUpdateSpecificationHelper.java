package commons;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Manages the LocalUpdateSpecification file
 */
public class LocalUpdateSpecificationHelper {
    private static final String LOCAL_UPDATE_DATA_FILE = "update" + File.separator +
                                                         "UpdateSpecification";

    public static boolean hasLocalUpdateSpecFile() {
        return FileUtil.isFileExists(LOCAL_UPDATE_DATA_FILE);
    }

    public static String getLocalUpdateSpecFilepath() {
        return LOCAL_UPDATE_DATA_FILE;
    }

    public static void clearLocalUpdateSpecFile() throws IOException {
        FileUtil.deleteFileIfExists(LOCAL_UPDATE_DATA_FILE);
    }

    public static void saveLocalUpdateSpecFile(List<String> affectedFiles) throws IOException {
        LocalUpdateSpecification localUpdateSpecification = new LocalUpdateSpecification(affectedFiles);

        FileUtil.serializeObjectToJsonFile(new File(LOCAL_UPDATE_DATA_FILE), localUpdateSpecification);
    }

    /**
     * @return first item is destination folder, the rest are affected files
     */
    public static List<String> readLocalUpdateSpecFile(String filepath) throws IOException {
        return FileUtil.deserializeObjectFromJsonFile(new File(filepath), LocalUpdateSpecification.class)
                .getLocalFilesToBeUpdated();
    }
}
