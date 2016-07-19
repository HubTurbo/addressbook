package address.updater;

import commons.UpdaterUtil;

import java.io.IOException;

public class UpdaterUpgrader {
    private static final int MAX_RETRIES = 10;
    private static final int WAIT_TIME = 2000;
    private String updaterFileName;

    public UpdaterUpgrader(String updaterFileName) {
        this.updaterFileName = updaterFileName;
    }

    public void upgradeUpdater() throws IOException {
        UpdaterUtil.updateFile("update", updaterFileName, MAX_RETRIES, WAIT_TIME);
    }
}
