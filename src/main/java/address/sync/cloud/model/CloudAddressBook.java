package address.sync.cloud.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the addressbook model used by the cloud
 *
 * This class should NOT be used by other components aside from RemoteService
 */
@XmlRootElement(name = "cloudaddressbook")
public class CloudAddressBook {
    String name;
    List<CloudPerson> personsList;
    List<CloudTag> tagsList;

    public CloudAddressBook() {
        this.personsList = new ArrayList<>();
        this.tagsList = new ArrayList<>();
    }

    public CloudAddressBook(String name) {
        this();
        this.name = name;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setPersonsList(List<CloudPerson> personsList) {
        this.personsList = personsList;
    }

    public void setTagsList(List<CloudTag> tagsList) {
        this.tagsList = tagsList;
    }

    public boolean equals(Object o) {
        if (!(o instanceof CloudAddressBook)) return false;

        CloudAddressBook cloudAddressBook = (CloudAddressBook) o;
        return cloudAddressBook.getAllPersons().equals(personsList) &&
                cloudAddressBook.getAllTags().equals(tagsList);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (personsList != null ? personsList.hashCode() : 0);
        result = 31 * result + (tagsList != null ? tagsList.hashCode() : 0);
        return result;
    }
}
