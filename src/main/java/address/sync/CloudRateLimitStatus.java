package address.sync;

import address.util.LoggerManager;
import address.util.TickingTimer;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public class CloudRateLimitStatus {
    private static final Logger logger = LoggerManager.getLogger(CloudRateLimitStatus.class);

    private int quotaLimit;
    private int quotaRemaining;
    private long quotaReset;
    private TickingTimer timer;

    public CloudRateLimitStatus(int quotaGiven) {
        this.quotaLimit = quotaGiven;
        this.quotaRemaining = quotaGiven;
        this.quotaReset = getNextResetTime();
    }

    public CloudRateLimitStatus(int quotaLimit, int quotaRemaining, long quotaReset) {
        this.quotaLimit = quotaLimit;
        this.quotaRemaining = quotaRemaining;
        this.quotaReset = quotaReset;
    }

    public CloudRateLimitStatus(int quotaGiven, long quotaReset) {
        this(quotaGiven, quotaGiven, quotaReset);
    }

    CloudRateLimitStatus() {
    }

    private void printTimeLeft(int timeLeft) {
        if (timeLeft % 60 == 0) logger.info(timeLeft + " seconds remaining to quota reset.");
    }

    private ZoneOffset getSystemTimezone() {
        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneOffset.systemDefault());
        return zonedDateTime.getOffset();
    }

    private long getNextResetTime() {
        LocalDateTime curTime = LocalDateTime.now();
        LocalDateTime nearestHour = LocalDateTime.of(
                curTime.getYear(), curTime.getMonth(), curTime.getDayOfMonth(), curTime.getHour() + 1,
                0, 0, 0);

        return nearestHour.toEpochSecond(getSystemTimezone());
    }

    private void resetQuotaAndRestartTimer() {
        long nextResetTime = getNextResetTime();
        this.quotaReset = nextResetTime;
        this.quotaRemaining = quotaLimit;
        restartQuotaTimer();
    }

    public void restartQuotaTimer() {
        if (timer != null) {
            timer.stop();
        }
        int timeout = (int) (quotaReset - LocalDateTime.now().toEpochSecond(getSystemTimezone()));
        timer = new TickingTimer("Cloud Quota Reset Time", timeout, this::printTimeLeft,
                this::resetQuotaAndRestartTimer, TimeUnit.SECONDS);
        timer.start();
    }

    public int getQuotaLimit() {
        return quotaLimit;
    }

    void setQuotaLimit(int quotaLimit) {
        this.quotaLimit = quotaLimit;
    }

    public int getQuotaRemaining() {
        return quotaRemaining;
    }

    void setQuotaRemaining(int quotaRemaining) {
        this.quotaRemaining = quotaRemaining;
    }

    public long getQuotaReset() {
        return quotaReset;
    }

    void setQuotaResetTime(long quotaResetTime) {
        this.quotaReset = quotaResetTime;
    }

    public void useQuota(int amount) {
        quotaRemaining -= amount;
    }
}
