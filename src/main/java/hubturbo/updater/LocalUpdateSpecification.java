package hubturbo.updater;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores information of resources to be updated
 */
public class LocalUpdateSpecification {
    private List<String> localFilesToBeUpdated;

    public LocalUpdateSpecification() {
        localFilesToBeUpdated = new ArrayList<>();
    }

    public LocalUpdateSpecification(List<String> localFilesToBeUpdated) {
        this.localFilesToBeUpdated = localFilesToBeUpdated;
    }

    public void setLocalFilesToBeUpdated(List<String> localFilesToBeUpdated) {
        this.localFilesToBeUpdated = localFilesToBeUpdated;
    }

    public List<String> getLocalFilesToBeUpdated() {
        return localFilesToBeUpdated;
    }
}
