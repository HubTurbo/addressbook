package address.sync.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "cloudaddressbook")
public class CloudAddressBook {
    List<CloudPerson> personsList;
    List<CloudTag> tagsList;

    public CloudAddressBook() {
        this.personsList = new ArrayList<>();
        this.tagsList = new ArrayList<>();
    }

    public CloudAddressBook(List<CloudPerson> personsList, List<CloudTag> tagsList) {
        this.personsList = personsList;
        this.tagsList = tagsList;
    }

    @XmlElement(name = "cloudpersons")
    public List<CloudPerson> getAllPersons() {
        return this.personsList;
    }

    @XmlElement(name = "cloudtags")
    public List<CloudTag> getAllTags() {
        return this.tagsList;
    }

}
