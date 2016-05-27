package address.sync;

public class RateLimitStatus {
    int quotaLimit;
    int quotaRemaining;
    long quotaReset;

    RateLimitStatus(int quotaLimit, int quotaRemaining, long quotaReset) {
        this.quotaLimit = quotaLimit;
        this.quotaRemaining = quotaRemaining;
        this.quotaReset = quotaReset;
    }

    RateLimitStatus() {
    }

    public int getQuotaLimit() {
        return quotaLimit;
    }

    public void setQuotaLimit(int quotaLimit) {
        this.quotaLimit = quotaLimit;
    }

    public int getQuotaRemaining() {
        return quotaRemaining;
    }

    public void setQuotaRemaining(int quotaRemaining) {
        this.quotaRemaining = quotaRemaining;
    }

    public long getQuotaReset() {
        return quotaReset;
    }

    public void setQuotaResetTime(long quotaResetTime) {
        this.quotaReset = quotaResetTime;
    }

    public void useQuota(int amount) {
        quotaRemaining -= amount;
    }
}
