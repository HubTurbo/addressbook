package address.sync.cloud.request;

import address.sync.cloud.CloudFileHandler;
import address.sync.cloud.CloudRateLimitStatus;
import address.util.AppLogger;
import address.util.LoggerManager;

public abstract class Request implements Runnable {
    private static final AppLogger logger = LoggerManager.getLogger(Request.class);
    protected CloudFileHandler cloudFileHandler;
    protected CloudRateLimitStatus cloudRateLimitStatus;
    public void setFileHandler(CloudFileHandler cloudFileHandler) {
        this.cloudFileHandler = cloudFileHandler;
    }

    public void setCloudRateLimitStatus(CloudRateLimitStatus cloudRateLimitStatus) {
        this.cloudRateLimitStatus = cloudRateLimitStatus;
    }

    protected boolean hasApiQuotaRemaining() {
        logger.info("Current quota left: {}", cloudRateLimitStatus.getQuotaRemaining());
        return cloudRateLimitStatus.getQuotaRemaining() > 0;
    }
}
