package address.updater;

import commons.FileUtil;
import commons.UpdaterUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is meant to replace the old updater with the new updater
 */
public class UpdaterUpgrader {
    private static final int MAX_RETRIES = 10;
    private static final int WAIT_TIME = 2000;
    private static final String UPDATE_DIR = "update";
    private static final String LIB_DIR = "lib";
    private static final String UPDATER_FILE_REGEX = "updater-\\d\\.\\d\\.\\d\\.jar";

    private String updaterFileName;

    public UpdaterUpgrader(String updaterFileName) {
        this.updaterFileName = updaterFileName;
    }

    /**
     * Attempts to upgrade the updater file
     *
     * Assumes that the new version has the same path from UPDATE_DIR
     *
     * @throws IOException
     */
    public void upgradeUpdater() throws IOException {
        FileUtil.deleteFile(findUpdaterFileName(LIB_DIR));
        UpdaterUtil.updateFile(UPDATE_DIR, updaterFileName, MAX_RETRIES, WAIT_TIME);
    }

    private String findUpdaterFileName(String dirPath) throws FileNotFoundException {
        File curDir = new File(dirPath);
        String[] curDirFilesNames = curDir.list();
        if (curDirFilesNames == null) assert false : "Not given a directory to check for updater!";
        return dirPath + File.separator + getFileNameOfRegexMatch(curDirFilesNames, UPDATER_FILE_REGEX);
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
        throw new FileNotFoundException("Updater file not found!");
    }
}
