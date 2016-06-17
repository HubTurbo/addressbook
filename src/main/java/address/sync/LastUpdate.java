package address.sync;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

public class LastUpdate {
    // TODO volatile?
    HashMap<Integer, String> eTags;
    LocalDateTime lastUpdatedAt;

    LastUpdate() {
        eTags = new HashMap<>();
    }

    public Optional<String> getETag(int pageNumber) {
        return Optional.ofNullable(eTags.get(pageNumber));
    }

    public void setETag(Integer pageNumber, String eTag) {
        eTags.put(pageNumber, eTag);
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
