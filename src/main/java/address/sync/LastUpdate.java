package address.sync;

import java.time.LocalDateTime;

public class LastUpdate {
    // TODO volatile?
    String eTag;
    LocalDateTime lastUpdatedAt;

    public String getETag() {
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
