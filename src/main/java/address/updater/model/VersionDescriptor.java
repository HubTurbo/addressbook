package address.updater.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Associates file changes in a version
 */
public class VersionDescriptor implements Comparable<VersionDescriptor> {
    @JsonProperty("version")
    private int versionNumber;

    @JsonProperty("filesToBeUpdated")
    private ArrayList<FileUpdateDescriptor> fileUpdateDescriptors;

    public VersionDescriptor() {}

    public VersionDescriptor(int versionNumber, ArrayList<FileUpdateDescriptor> fileUpdateDescriptors) {
        this.versionNumber = versionNumber;
        this.fileUpdateDescriptors = fileUpdateDescriptors;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public ArrayList<FileUpdateDescriptor> getFileUpdateDescriptors() {
        return fileUpdateDescriptors;
    }

    @Override
    public int compareTo(VersionDescriptor other) {
        return (this.versionNumber != other.versionNumber) ? (this.versionNumber - other.versionNumber) : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !(obj instanceof VersionDescriptor)) return false;
        return this.versionNumber == ((VersionDescriptor) obj).getVersionNumber();
    }

    @Override
    public int hashCode() {
        return this.versionNumber;
    }
}
