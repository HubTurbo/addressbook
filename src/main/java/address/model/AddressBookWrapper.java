package address.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Helper class to wrap an address book. This is used for saving the
 * lists of persons and groups to XML.
 * 
 * Adapted and modified from Marco Jakob
 */
@XmlRootElement(name = "addressbook")
public class AddressBookWrapper {

    private List<Person> persons = new ArrayList<>(); // so empty lists from file will not be null
    private List<ContactGroup> groups = new ArrayList<>(); // ditto

    @XmlElement(name = "persons")
    public List<Person> getPersons() {
        return persons;
    }

    @XmlElement(name = "groups")
    public List<ContactGroup> getGroups() {
        return groups;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

    public void setGroups(List<ContactGroup> groups) {
        this.groups = groups;
    }

    public boolean containsDuplicates() {
        final Set<Person> personSet = new HashSet<>();
        final Set<ContactGroup> groupSet = new HashSet<>();
        for (Person p : persons) {
            if (!personSet.add(p)) return true;
        }
        for (ContactGroup cg : groups) {
            if (!groupSet.add(cg)) return true;
        }
        return false;
    }
}
