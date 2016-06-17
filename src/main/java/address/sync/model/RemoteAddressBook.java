package address.sync.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the addressbook model used by any remote server
 *
 * This class should NOT be used by other components aside from RemoteService
 */
@XmlRootElement(name = "remoteaddressbook")
public class RemoteAddressBook {
    String name;
    List<RemotePerson> personsList;
    List<RemoteTag> tagsList;

    public RemoteAddressBook() {
        this.personsList = new ArrayList<>();
        this.tagsList = new ArrayList<>();
    }

    public RemoteAddressBook(String name) {
        this();
        this.name = name;
    }

    public RemoteAddressBook(String name, List<RemotePerson> personsList, List<RemoteTag> tagsList) {
        this.name = name;
        this.personsList = personsList;
        this.tagsList = tagsList;
    }

    @XmlElement(name = "name")
    public String getName() {
        return this.name;
    }

    @XmlElement(name = "remotepersons")
    public List<RemotePerson> getAllPersons() {
        return this.personsList;
    }

    @XmlElement(name = "remotetags")
    public List<RemoteTag> getAllTags() {
        return this.tagsList;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPersonsList(List<RemotePerson> personsList) {
        this.personsList = personsList;
    }

    public void setTagsList(List<RemoteTag> tagsList) {
        this.tagsList = tagsList;
    }

    public boolean equals(Object o) {
        if (!(o instanceof RemoteAddressBook)) return false;

        RemoteAddressBook remoteAddressBook = (RemoteAddressBook) o;
        return remoteAddressBook.getAllPersons().equals(personsList) &&
                remoteAddressBook.getAllTags().equals(tagsList);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (personsList != null ? personsList.hashCode() : 0);
        result = 31 * result + (tagsList != null ? tagsList.hashCode() : 0);
        return result;
    }
}
