package address.updater;

import address.util.FileUtil;
import address.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Accesses LocalUpdateSpecification
 */
public class LocalUpdateSpecificationHelper {
    private static final String LOCAL_UPDATE_DATA_FILE = UpdateManager.UPDATE_DIR + File.separator +
                                                         "UpdateSpecification";

    public static String getLocalUpdateSpecFilepath() {
        return LOCAL_UPDATE_DATA_FILE;
    }

    public static void clearLocalUpdateSpecFile() throws IOException {
        // delete local update data file here
        File localUpdateSpecFile = new File(LOCAL_UPDATE_DATA_FILE);

        if (localUpdateSpecFile.exists()) {
            Files.delete(localUpdateSpecFile.toPath());
        }
    }

    public static void saveLocalUpdateSpecFile(List<String> affectedFiles) throws IOException {
        LocalUpdateSpecification localUpdateSpecification = new LocalUpdateSpecification(affectedFiles);

        FileUtil.writeToFile(new File(LOCAL_UPDATE_DATA_FILE), JsonUtil.toJsonString(localUpdateSpecification));
    }

    /**
     * @return first item is destination folder, the rest are affected files
     */
    public static List<String> readLocalUpdateSpecFile(String filepath) throws IOException {
        return JsonUtil.fromJsonString(FileUtil.readFromFile(new File(filepath)), LocalUpdateSpecification.class)
                .getLocalFilesToBeUpdated();
    }
}
