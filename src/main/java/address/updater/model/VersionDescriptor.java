package address.updater.model;

import address.util.Version;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Associates file changes in a version
 */
public class VersionDescriptor implements Comparable<VersionDescriptor> {
    private Version version;

    @JsonProperty("filesToBeUpdated")
    private ArrayList<FileUpdateDescriptor> fileUpdateDescriptors;

    public VersionDescriptor() {}

    public VersionDescriptor(Version version, ArrayList<FileUpdateDescriptor> fileUpdateDescriptors) {
        this.version = version;
        this.fileUpdateDescriptors = fileUpdateDescriptors;
    }

    public Version getVersion() {
        return version;
    }

    public ArrayList<FileUpdateDescriptor> getFileUpdateDescriptors() {
        return fileUpdateDescriptors;
    }

    @Override
    public int compareTo(VersionDescriptor other) {
        return this.version.compareTo(other.version);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !(obj instanceof VersionDescriptor)) return false;
        return this.version == ((VersionDescriptor) obj).getVersion();
    }
}
