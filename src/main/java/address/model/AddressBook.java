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

    private final List<Person> persons = new ArrayList<>();
    private final List<Tag> tags = new ArrayList<>();

    public AddressBook() {}

    public AddressBook(AddressBook toBeCopied) {
        this(toBeCopied.getPersons(), toBeCopied.getTags());
    }

    public AddressBook(List<Person> persons, List<Tag> tags) {
        setPersons(persons);
        setTags(tags);
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
        this.persons.clear();
        this.persons.addAll(persons);
    }

    public void setTags(List<Tag> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }

    //TODO: refine later
    public void addPerson(Person p){
        persons.add(p);
    }

    //TODO: refine later
    public void addTag(Tag t){
        tags.add(t);
    }

    // Deprecated (to be removed when no-dupe property is properly enforced
    public boolean containsDuplicates() {
        return !UniqueData.itemsAreUnique(persons) || !UniqueData.itemsAreUnique(tags);
    }

    @Override
    public String toString(){
        //TODO: refine later
        return "Persons : " + persons.size() + "\n"
                + "Tags : " + tags.size();

    }
}
