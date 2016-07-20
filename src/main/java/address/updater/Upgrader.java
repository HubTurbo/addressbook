package address.updater;

import commons.FileUtil;
import commons.UpdaterUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This class is meant to replace the old components with the new components
 */
public class Upgrader {
    private static final int MAX_RETRIES = 10;
    private static final int WAIT_TIME = 2000;
    private static final String UPDATE_DIR = "update";
    private static final String LIB_DIR = "lib";
    private static final String CUR_DIR = ".";
    private static final String UPDATER_FILE_REGEX = "updater-\\d\\.\\d\\.\\d\\.jar";
    private static final String LAUNCHER_FILE_REGEX = "addressbook-V\\d\\.\\d\\.\\d(ea)?\\.jar";

    private String updaterFileName;
    private String launcherFileName;

    public Upgrader(String launcherFileName, String updaterFileName) {
        this.launcherFileName = launcherFileName;
        this.updaterFileName = updaterFileName;
    }

    /**
     * Attempts to upgrade the updater file if required
     *
     * Assumes that the new version has the same path from UPDATE_DIR
     *
     * @throws IOException
     */
    public void upgradeUpdaterIfRequired() throws IOException {
        if (updaterFileName == null) return;

        UpdaterUtil.deleteFile(findComponentFileName(LIB_DIR, UPDATER_FILE_REGEX), MAX_RETRIES, WAIT_TIME);
        UpdaterUtil.updateFile(UPDATE_DIR, updaterFileName, MAX_RETRIES, WAIT_TIME);
    }

    /**
     * Attempts to upgrade the launcher file
     *
     * Assumes that the new version has the same path from UPDATE_DIR
     *
     * @throws IOException
     */
    public void upgradeLauncher() throws IOException {
        UpdaterUtil.deleteFile(findComponentFileName(CUR_DIR, LAUNCHER_FILE_REGEX), MAX_RETRIES, WAIT_TIME);
        UpdaterUtil.updateFile(UPDATE_DIR, launcherFileName, MAX_RETRIES, WAIT_TIME);
    }

    /**
     * Finds, in the directory at dirPath, a file name that matches the given regex
     *
     * @param dirPath
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
     * @param curDirFilesNames
     * @param regex
     * @return file name of matching file
     * @throws FileNotFoundException if no file name matches the given regex
     */
    private String getFileNameOfRegexMatch(String[] curDirFilesNames, String regex) throws FileNotFoundException {
        for (String fileName : curDirFilesNames) {
            if (fileName.matches(regex)) return fileName;
        }
        throw new FileNotFoundException("File not found!");
    }
}
