package address.sync;

import java.util.Optional;

public class ExtractedCloudResponse<V> {
    Optional<V> data;
    RateLimitStatus rateLimitStatus;
    int responseCode;

    ExtractedCloudResponse(int responseCode, RateLimitStatus rateLimitStatus) {
        this.responseCode = responseCode;
        this.rateLimitStatus = rateLimitStatus;
    }

    ExtractedCloudResponse(int responseCode, RateLimitStatus rateLimitStatus, V data) {
        this(responseCode, rateLimitStatus);
        this.data = Optional.ofNullable(data);
    }
    ExtractedCloudResponse(){
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public RateLimitStatus getRateLimitStatus() {
        return rateLimitStatus;
    }

    public void setRateLimitStatus(RateLimitStatus rateLimitStatus) {
        this.rateLimitStatus = rateLimitStatus;
    }

    public Optional<V> getData() {
        return data;
    }

    public void setData(V data) {
        this.data = Optional.ofNullable(data);
    }
}
