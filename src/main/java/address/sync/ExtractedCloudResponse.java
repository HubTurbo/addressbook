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
    String eTag;

    // temporarily copied from CloudSimulator, to be refactored
    private ZoneOffset getSystemTimezone() {
        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneOffset.systemDefault());
        return zonedDateTime.getOffset();
    }

    ExtractedCloudResponse(int responseCode, String eTag, int quotaLimit, int quotaRemaining, long quotaResetTimeEpochSeconds) {
        this(responseCode);
        this.eTag = eTag;
        this.quotaLimit = quotaLimit;
        this.quotaRemaining = quotaRemaining;
        this.quotaResetTime = LocalDateTime.ofEpochSecond(quotaResetTimeEpochSeconds, 0, getSystemTimezone());
    }

    ExtractedCloudResponse(int responseCode, String eTag, int quotaLimit, int quotaRemaining, long quotaResetTimeEpochSeconds, V data) {
        this(responseCode, eTag, quotaLimit, quotaRemaining, quotaResetTimeEpochSeconds);
        this.data = Optional.ofNullable(data);
    }

    ExtractedCloudResponse(int responseCode) {
        this.responseCode = responseCode;
        this.data = Optional.empty();
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

    public String getETag() {
        return eTag;
    }

    public LocalDateTime getQuotaResetTime() {
        return quotaResetTime;
    }

    public Optional<V> getData() {
        return data;
    }
}
