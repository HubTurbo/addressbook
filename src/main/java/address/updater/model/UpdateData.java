package address.updater.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Lists file changes on each version
 */
public class UpdateData {
    @JsonProperty("updateData")
    private ArrayList<VersionDescriptor> versionFileChanges = new ArrayList<>();

    public void setVersionFileChanges(ArrayList<VersionDescriptor> versionFileChanges) {
        this.versionFileChanges = versionFileChanges;
    }

    public ArrayList<VersionDescriptor> getAllVersionFileChanges() {
        return versionFileChanges;
    }
}
