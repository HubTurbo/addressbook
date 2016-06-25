package address.updater;

import address.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stores data of update to be applied locally
 *
 * File Format:
 * line 1 | [path of affected file 1]
 * line 2 | [path of affected file 2]
 * etc.
 */
public class LocalUpdateSpecificationHelper {
    //TODO: separate the object from the helper e.g. LocalUpdateSpecification + LocalUpdateSpecificationHelper
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
        StringBuilder fileContent = new StringBuilder();

        for (String line : affectedFiles) {
            fileContent.append(line).append(String.format("%n"));
        }

        FileUtil.writeToFile(new File(LOCAL_UPDATE_DATA_FILE), fileContent.toString().trim());
    }

    /**
     * @return first item is destination folder, the rest are affected files
     */
    public static List<String> readLocalUpdateSpecFile(String filepath) throws IOException {
        String fileContent = FileUtil.readFromFile(new File(filepath));

        return new ArrayList<>(Arrays.asList(fileContent.split("\\s+")));
    }
}
