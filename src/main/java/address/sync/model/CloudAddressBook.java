package address.sync.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is meant to wrap cloud persons and cloud tags in the cloud for easier storage
 *
 * This class should NOT be used by other components aside from CloudService
 */
@XmlRootElement(name = "cloudaddressbook")
public class CloudAddressBook {
    String name;
    List<CloudPerson> personsList;
    List<CloudTag> tagsList;

    public CloudAddressBook(String name) {
        this.name = name;
        this.personsList = new ArrayList<>();
        this.tagsList = new ArrayList<>();
    }

    public CloudAddressBook(String name, List<CloudPerson> personsList, List<CloudTag> tagsList) {
        this.name = name;
        this.personsList = personsList;
        this.tagsList = tagsList;
    }

    @XmlElement(name = "name")
    public String getName() {
        return this.name;
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
