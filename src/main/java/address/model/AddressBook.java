package address.model;

import java.util.*;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wraps all data at the address-book level
 *
 * Duplicates are not allowed (by .equals comparison)
 * TODO: truly enforce set property through code (Sets, XML Schemas)
 */

@XmlRootElement(name = "addressbook")
public class AddressBook {

    private List<Person> persons = new ArrayList<>();
    private List<ContactGroup> groups = new ArrayList<>();

    @XmlElement(name = "persons")
    public List<Person> getPersons() {
        return persons.stream()
                .map(Person::new)
                .collect(Collectors.toList());
    }

    @XmlElement(name = "groups")
    public List<ContactGroup> getGroups() {
        return groups.stream()
                .map(ContactGroup::new)
                .collect(Collectors.toList());
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

    public void setGroups(List<ContactGroup> groups) {
        this.groups = groups;
    }

    // Deprecated (to be removed when no-dupe property is properly enforced
    public boolean containsDuplicates() {
        return UniqueData.itemsAreUnique(persons) && UniqueData.itemsAreUnique(groups);
    }
}
