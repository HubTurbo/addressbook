package address.updater;

import address.util.AppLogger;
import address.util.LoggerManager;
import commons.UpdaterUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is meant to replace the old components with new components
 */
public class Upgrader {
    private static final AppLogger logger = LoggerManager.getLogger(Upgrader.class);
    private static final String UPDATE_DIR = "update";
    private static final String LIB_DIR = "lib";
    private static final String CUR_DIR = ".";
    private static final String UPDATER_FILE_REGEX = "updater-\\d\\.\\d\\.\\d\\.jar";
    private static final String LAUNCHER_FILE_REGEX = "addressbook-V\\d\\.\\d\\.\\d(ea)?\\.jar";

    private String updaterFileName;
    private String launcherFileName;

    private final ExecutorService pool = Executors.newSingleThreadExecutor();

    public Upgrader(String launcherFileName, String updaterFileName) {
        this.launcherFileName = launcherFileName;
        this.updaterFileName = updaterFileName;
    }

    /**
     * Attempts to upgrade the updater file if required
     *
     * Assumes that the new version has the same path starting from UPDATE_DIR
     *
     * @throws IOException
     */
    public void upgradeUpdaterIfRequired() throws IOException {
        pool.execute(() -> {
            if (updaterFileName == null) return;
            try {
                UpdaterUtil.deleteFile(findComponentFileName(LIB_DIR, UPDATER_FILE_REGEX));
                UpdaterUtil.updateFile(UPDATE_DIR, updaterFileName);
            } catch (IOException e) {
                logger.warn("Error updating updater: {}", e);
            }
        });
    }

    /**
     * Attempts to upgrade the launcher file
     *
     * Assumes that the new version has the same path starting from UPDATE_DIR
     *
     * @throws IOException
     */
    public void upgradeLauncher() throws IOException {
        pool.execute(() -> {
            try {
                UpdaterUtil.deleteFile(findComponentFileName(CUR_DIR, LAUNCHER_FILE_REGEX));
                UpdaterUtil.updateFile(UPDATE_DIR, launcherFileName);
            } catch (IOException e) {
                logger.warn("Error updating launcher: {}", e);
            }
        });
    }

    /**
     * Finds, in the directory at dirPath, a file name that matches the given regex
     *
     * @param dirPath must be a valid directory
     * @param regex
     * @return
     * @throws FileNotFoundException
     */
    private String findComponentFileName(String dirPath, String regex) throws FileNotFoundException {
        File curDir = new File(dirPath);
        String[] curDirFilesNames = curDir.list();
        if (curDirFilesNames == null) assert false : "Not given a directory to check for component!";
        return dirPath + File.separator + getFileNameOfRegexMatch(curDirFilesNames, regex);
    }

    /**
     * Attempts to get the file which matching file name as the given regex
     *
     * @param fileNames
     * @param regex
     * @return file name of matching file
     * @throws FileNotFoundException if no file name matches the given regex
     */
    private String getFileNameOfRegexMatch(String[] fileNames, String regex) throws FileNotFoundException {
        for (String fileName : fileNames) {
            if (fileName.matches(regex)) return fileName;
        }
        throw new FileNotFoundException("File matching " + regex + " not found!");
    }
}
