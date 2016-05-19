package address.updater.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

/**
 * Lists file changes on each version
 */
@XmlRootElement(name = "updatedata")
@XmlSeeAlso({VersionDescriptor.class})
@XmlAccessorType(XmlAccessType.FIELD)
public class UpdateData {
    @XmlElementWrapper(name = "versions")
    @XmlElement(name = "versiondescriptor")
    private ArrayList<VersionDescriptor> versionFileChanges = new ArrayList<>();

    public void setVersionFileChanges(ArrayList<VersionDescriptor> versionFileChanges) {
        this.versionFileChanges = versionFileChanges;
    }

    public ArrayList<VersionDescriptor> getAllVersionFileChanges() {
        return (ArrayList<VersionDescriptor>) versionFileChanges.clone();
    }
}
