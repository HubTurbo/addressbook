package commons;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UpdaterUtil {
    private static final int DEFAULT_MAX_RETRIES = 10;
    private static final int DEFAULT_WAIT_TIME_BETWEEN_RETRIES = 2000;
    /**
     * Attempts to move the file from source to dest
     * Source file will not be kept and destination file will be overwritten
     *
     * @param source
     * @param dest
     * @return true if successful
     */
    public static boolean applyUpdate(Path source, Path dest) {
        try {
            FileUtil.moveFile(source, dest, true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Attempts to sleep for a specified period
     *
     * @param sleepDurationInMilliseconds
     */
    public static void sleepFor(int sleepDurationInMilliseconds) {
        try {
            Thread.sleep(sleepDurationInMilliseconds);
        } catch (InterruptedException e) {
            System.out.println("Error sleeping thread for: " + sleepDurationInMilliseconds);
        }
    }

    /**
     * Attempts to replace the file with a newer version
     *
     * In some platforms (Windows in particular), JAR file cannot be modified if it was executed and
     * the process it created has not ended yet. As such, we will make several tries with wait.
     *
     * Source file will not be kept
     *
     * @param sourceDir the (temp) directory which contains the newer version of the file
     * @param fileToUpdatePath the path of the resulting file from the project directory
     * @param maxRetries maximum number of retries
     * @param waitTime amount of time to wait between retries
     * @throws IOException
     */
    public static void updateFile(String sourceDir, String fileToUpdatePath, int maxRetries, int waitTime) throws IOException {
        Path source = Paths.get(sourceDir, fileToUpdatePath);
        Path dest = Paths.get(fileToUpdatePath);
        createFileAndParentDirs(dest);

        for (int i = 0; i < maxRetries; i++) {
            if (applyUpdate(source, dest)) return;
            if (i != maxRetries - 1) sleepFor(waitTime);
        }
        throw new IOException("File " + fileToUpdatePath + " cannot be updated. Most likely it is in use by another process.");
    }

    /**
     * Attempts to delete the target file
     *
     * In some platforms (Windows in particular), JAR file cannot be modified if it was executed and
     * the process it created has not ended yet. As such, we will make several tries with wait.
     *
     * @param fileToDeletePath the file to delete from the project directory
     * @param maxRetries maximum number of retries
     * @param waitTime amount of time to wait between retries
     * @throws IOException
     */
    public static void deleteFile(String fileToDeletePath, int maxRetries, int waitTime) throws IOException {
        for (int i = 0; i < maxRetries; i++) {
            if (applyDelete(fileToDeletePath)) return;
            if (i != maxRetries - 1) sleepFor(waitTime);
        }
        throw new IOException("File " + fileToDeletePath + " cannot be deleted. Most likely it is in use by another process.");
    }

    /**
     * Attempts to update the target file with default number of retries and wait time
     *
     * @param sourceDir
     * @param fileToUpdatePath
     * @throws IOException
     */
    public static void updateFile(String sourceDir, String fileToUpdatePath) throws IOException {
        updateFile(sourceDir, fileToUpdatePath, DEFAULT_MAX_RETRIES, DEFAULT_WAIT_TIME_BETWEEN_RETRIES);
    }

    /**
     * Attempts to delete the target file with default number of retries and wait time
     * @param fileToDeletePath
     * @throws IOException
     */
    public static void deleteFile(String fileToDeletePath) throws IOException {
        deleteFile(fileToDeletePath, DEFAULT_MAX_RETRIES, DEFAULT_WAIT_TIME_BETWEEN_RETRIES);
    }

    private static boolean applyDelete(String filePath) {
        try {
            FileUtil.deleteFile(filePath);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void createFileAndParentDirs(Path dest) throws IOException {
        if (!FileUtil.isFileExists(dest.toString())) {
            FileUtil.createParentDirsOfFile(dest.toFile());
        }
    }
}
