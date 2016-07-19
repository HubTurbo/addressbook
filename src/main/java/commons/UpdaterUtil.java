package commons;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UpdaterUtil {
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
     */
    public static void updateFile(String sourceDir, String fileToUpdate, int maxRetries, int waitTime) throws IOException {
        Path source = Paths.get(sourceDir, fileToUpdate);
        Path dest = Paths.get(fileToUpdate);
        createFileAndParentDirs(dest);

        for (int i = 0; i < maxRetries; i++) {
            if (applyUpdate(source, dest)) return;
            if (i != maxRetries - 1) sleepFor(waitTime);
        }
        throw new IOException("Jar file cannot be updated. Most likely it is in use by another process.");
    }

    private static void createFileAndParentDirs(Path dest) throws IOException {
        if (!FileUtil.isFileExists(dest.toString())) {
            FileUtil.createParentDirsOfFile(dest.toFile());
        }
    }
}
