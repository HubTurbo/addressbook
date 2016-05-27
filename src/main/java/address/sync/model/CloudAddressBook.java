package address.sync.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "cloudaddressbook")
public class CloudAddressBook {
    List<CloudPerson> personsList;
    List<CloudGroup> groupsList;

    CloudAddressBook(List<CloudPerson> personsList, List<CloudGroup> groupsList) {
        this.personsList = personsList;
        this.groupsList = groupsList;
    }

    @XmlElement(name = "cloudpersons")
    public List<CloudPerson> getAllPersons() {
        return this.personsList;
    }

    @XmlElement(name = "cloudgroups")
    public List<CloudGroup> getAllGroups() {
        return this.groupsList;
    }

}
