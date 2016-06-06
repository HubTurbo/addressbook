package address.sync;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.TimeZone;

public class ExtractedCloudResponse<V> {
    private Optional<V> data;
    private int quotaLimit;
    private int quotaRemaining;
    private LocalDateTime quotaResetTime;
    private int responseCode;

    // temporarily copied from CloudSimulator, to be refactored
    private ZoneOffset getSystemTimezone() {
        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneOffset.systemDefault());
        return zonedDateTime.getOffset();
    }

    ExtractedCloudResponse(int responseCode, int quotaLimit, int quotaRemaining, long quotaResetTimeEpochSeconds) {
        this.responseCode = responseCode;
        this.quotaLimit = quotaLimit;
        this.quotaRemaining = quotaRemaining;
        this.quotaResetTime = LocalDateTime.ofEpochSecond(quotaResetTimeEpochSeconds, 0, getSystemTimezone());
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
