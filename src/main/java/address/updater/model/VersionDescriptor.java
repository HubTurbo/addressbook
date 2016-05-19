package address.updater.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

/**
 * Associates file changes in a version
 */
@XmlRootElement(name = "versiondescriptor")
@XmlSeeAlso({FileUpdateDescriptor.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class VersionDescriptor implements Comparable<VersionDescriptor> {
    private int versionNumber;

    @XmlElementWrapper(name = "filestobeupdated")
    @XmlElement(name = "fileupdatedescriptor")
    private ArrayList<FileUpdateDescriptor> fileList;

    /**
     * Required for XML conversion
     */
    public VersionDescriptor() {}

    public VersionDescriptor(int versionNumber, ArrayList<FileUpdateDescriptor> fileList) {
        this.versionNumber = versionNumber;
        this.fileList = fileList;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public ArrayList<FileUpdateDescriptor> getFileUpdateDescriptors() {
        return (ArrayList<FileUpdateDescriptor>) fileList.clone();
    }

    @Override
    public int compareTo(VersionDescriptor other) {
        return this.versionNumber != other.versionNumber ? this.versionNumber - other.versionNumber : 0;
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
