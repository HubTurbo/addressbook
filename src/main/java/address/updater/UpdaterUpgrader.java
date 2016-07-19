package address.updater;

import commons.FileUtil;
import commons.UpdaterUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdaterUpgrader {
    private static final int MAX_RETRIES = 10;
    private static final int WAIT_TIME = 2000;
    private String updaterFileName;
    private String UPDATER_FILE_REGEX = "updater-\\d\\.\\d\\.\\d\\.jar";

    public UpdaterUpgrader(String updaterFileName) {
        this.updaterFileName = updaterFileName;
    }


    private String findUpdaterFileName(String dirPath) throws FileNotFoundException {
        File curDir = new File(dirPath);
        String[] curDirFilesNames = curDir.list();
        if (curDirFilesNames == null) assert false : "Not given a directory to check for updater!";
        for (String fileName : curDirFilesNames) {
            Pattern pattern = Pattern.compile(UPDATER_FILE_REGEX);
            Matcher matcher = pattern.matcher(fileName);
            if (matcher.matches()) return dirPath + fileName;
        }
        throw new FileNotFoundException("Updater file not found!");
    }

    public void upgradeUpdater() throws IOException {
        FileUtil.deleteFile(findUpdaterFileName("lib/"));
        UpdaterUtil.updateFile("update", updaterFileName, MAX_RETRIES, WAIT_TIME);
    }
}
