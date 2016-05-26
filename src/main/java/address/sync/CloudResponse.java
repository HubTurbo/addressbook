package address.sync;

public class CloudResponse<V> {
    V data;
    int quotaLimit;
    int quotaRemaining;
    long quotaReset;

    public V getData() {
        return data;
    }

    public void setData(V data) {
        this.data = data;
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

    public void setQuotaReset(long quotaReset) {
        this.quotaReset = quotaReset;
    }
}
