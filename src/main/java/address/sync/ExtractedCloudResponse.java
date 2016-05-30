package address.sync;

import java.util.Optional;

public class ExtractedCloudResponse<V> {
    private Optional<V> data;
    private RateLimitStatus rateLimitStatus;
    private int responseCode;

    ExtractedCloudResponse(int responseCode, RateLimitStatus rateLimitStatus) {
        this.responseCode = responseCode;
        this.rateLimitStatus = rateLimitStatus;
    }

    ExtractedCloudResponse(int responseCode, RateLimitStatus rateLimitStatus, V data) {
        this(responseCode, rateLimitStatus);
        this.data = Optional.ofNullable(data);
    }

    ExtractedCloudResponse() {
    }

    public int getResponseCode() {
        return responseCode;
    }

    public RateLimitStatus getRateLimitStatus() {
        return rateLimitStatus;
    }

    public Optional<V> getData() {
        return data;
    }
}
