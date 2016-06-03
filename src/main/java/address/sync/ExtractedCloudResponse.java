package address.sync;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class ExtractedCloudResponse<V> {
    private Optional<V> data;
    private int quotaLimit;
    private int quotaRemaining;
    private LocalDateTime quotaResetTime;
    private int responseCode;

    ExtractedCloudResponse(int responseCode, int quotaLimit, int quotaRemaining, long quotaResetTimeEpochSeconds) {
        this.responseCode = responseCode;
        this.quotaLimit = quotaLimit;
        this.quotaRemaining = quotaRemaining;
        this.quotaResetTime = LocalDateTime.ofEpochSecond(quotaResetTimeEpochSeconds, 0, ZoneOffset.of(ZoneOffset.systemDefault().getId()));
    }

    ExtractedCloudResponse(int responseCode, int quotaLimit, int quotaRemaining, long quotaResetTimeEpochSeconds, V data) {
        this(responseCode, quotaLimit, quotaRemaining, quotaResetTimeEpochSeconds);
        this.data = Optional.ofNullable(data);
    }

    ExtractedCloudResponse() {
    }

    public int getResponseCode() {
        return responseCode;
    }

    public int getQuotaLimit() {
        return quotaLimit;
    }

    public int getQuotaRemaining() {
        return quotaRemaining;
    }

    public LocalDateTime getQuotaResetTime() {
        return quotaResetTime;
    }

    public Optional<V> getData() {
        return data;
    }
}
