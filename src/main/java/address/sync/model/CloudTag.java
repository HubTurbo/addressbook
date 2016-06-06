package address.sync.model;

import java.time.LocalDateTime;

public class CloudTag {
    private String name;
    private LocalDateTime lastUpdatedAt;

    public CloudTag() {
        setLastUpdatedAt(LocalDateTime.now());
    }

    public CloudTag(String name) {
        this.name = name;
        setLastUpdatedAt(LocalDateTime.now());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setLastUpdatedAt(LocalDateTime.now());
    }

    public void updatedBy(CloudTag updatedTag) {
        this.name = updatedTag.name;
        setLastUpdatedAt(LocalDateTime.now());
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    private void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public boolean isValid() {
        return name != null;
    }

    public boolean equals(Object o) {
        if (!(o instanceof CloudTag)) return false;
        if (o == null) return false;

        return name.equals(((CloudTag) o).getName());
    }

    public int hashCode() {
        return 42 + name.hashCode();
    }
}
