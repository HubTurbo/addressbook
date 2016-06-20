package address.sync;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

public class ExtractedRemoteResponse<V> {
    private Optional<V> data;
    private int quotaLimit;
    private int quotaRemaining;
    private LocalDateTime quotaResetTime;
    private int responseCode;
    private String eTag;
    private int prevPage;
    private int nextPage;
    private int firstPage;
    private int lastPage;


    // temporarily copied from CloudSimulator, to be refactored
    private ZoneOffset getSystemTimezone() {
        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneOffset.systemDefault());
        return zonedDateTime.getOffset();
    }

    public ExtractedRemoteResponse(int responseCode, String eTag, int quotaLimit, int quotaRemaining,
                                   long quotaResetTimeEpochSeconds) {
        this(responseCode);
        this.eTag = eTag;
        this.quotaLimit = quotaLimit;
        this.quotaRemaining = quotaRemaining;
        this.quotaResetTime = LocalDateTime.ofEpochSecond(quotaResetTimeEpochSeconds, 0, getSystemTimezone());
    }

    public ExtractedRemoteResponse(int responseCode, String eTag, int quotaLimit, int quotaRemaining,
                                   long quotaResetTimeEpochSeconds, V data) {
        this(responseCode, eTag, quotaLimit, quotaRemaining, quotaResetTimeEpochSeconds);
        this.data = Optional.ofNullable(data);
    }

    public ExtractedRemoteResponse(int responseCode) {
        this.responseCode = responseCode;
        this.data = Optional.empty();
    }

    ExtractedRemoteResponse() {
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

    public int getPrevPage() {
        return prevPage;
    }

    public void setPrevPage(int prevPage) {
        this.prevPage = prevPage;
    }

    public int getNextPage() {
        return nextPage;
    }

    public void setNextPage(int nextPage) {
        this.nextPage = nextPage;
    }

    public int getFirstPage() {
        return firstPage;
    }

    public void setFirstPage(int firstPage) {
        this.firstPage = firstPage;
    }

    public int getLastPage() {
        return lastPage;
    }

    public void setLastPage(int lastPage) {
        this.lastPage = lastPage;
    }
}
