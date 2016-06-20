package address.sync;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class LastUpdate<T> {
    private class UpdateInfo<V> {
        private String eTag;
        private List<V> resourceList;
        private UpdateInfo(String eTag, List<V> resourceList) {
            this.eTag = eTag;
            this.resourceList = resourceList;
        }

        private String getETag() {
            return eTag;
        }

        private List<V> getResourceList() {
            return resourceList;
        }
    }

    // TODO volatile?
    HashMap<Integer, UpdateInfo<T>> eTags;
    LocalDateTime lastUpdatedAt;
    List<T> resourceList;

    LastUpdate() {
        eTags = new HashMap<>();
    }

    public Optional<String> getETag(int pageNumber) {
        if (eTags.get(pageNumber) == null) return Optional.empty();
        return Optional.ofNullable(eTags.get(pageNumber).getETag());
    }

    public Optional<List<T>> getResourceList(int pageNumber) {
        if (eTags.get(pageNumber) == null) return Optional.empty();
        return Optional.ofNullable(eTags.get(pageNumber).getResourceList());
    }

    public void setUpdate(Integer pageNumber, String eTag, List<T> resourceList) {
        UpdateInfo<T> updateInfo = new UpdateInfo<>(eTag, resourceList);
        eTags.put(pageNumber, updateInfo);
    }

    public int getETagCount() {
        return eTags.size();
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
