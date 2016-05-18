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

    @XmlElementWrapper(name="filestobeupdated")
    @XmlElement(name="fileupdatedescriptor")
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

    public int compareTo(VersionDescriptor other) {
        return this.versionNumber != other.versionNumber ? this.versionNumber - other.versionNumber : 0;
    }
}
