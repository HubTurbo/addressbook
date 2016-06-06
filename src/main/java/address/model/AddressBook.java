package address.model;

import address.model.datatypes.Person;
import address.model.datatypes.Tag;
import address.model.datatypes.UniqueData;

import java.util.*;

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
    private List<Tag> tags = new ArrayList<>();

    //TODO: remove this empty constructor
    public AddressBook (){
    }

    public AddressBook (List<Person> persons, List<Tag> tags){
        this.persons = persons;
        this.tags = tags;
    }

    @XmlElement(name = "persons")
    public List<Person> getPersons() {
        return persons;
    }

    @XmlElement(name = "tags")
    public List<Tag> getTags() {
        return tags;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    // Deprecated (to be removed when no-dupe property is properly enforced
    public boolean containsDuplicates() {
        return !UniqueData.itemsAreUnique(persons) || !UniqueData.itemsAreUnique(tags);
    }
}
