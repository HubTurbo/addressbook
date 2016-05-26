package address.sync;

import java.util.Optional;

public class CloudResponse<V> extends RateLimitStatus {
    Optional<V> data;
    int quotaLimit;
    int quotaRemaining;
    long quotaReset;
    int responseCode;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public Optional<V> getData() {
        return data;
    }

    public void setData(V data) {
        this.data = Optional.ofNullable(data);
    }
}
