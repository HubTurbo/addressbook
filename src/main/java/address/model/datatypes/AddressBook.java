package address.model.datatypes;

import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.tag.Tag;
import address.util.collections.UnmodifiableObservableList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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
public class AddressBook implements ReadOnlyAddressBook {

    @JsonIgnore private final ObservableList<Person> persons;
    @JsonIgnore private final ObservableList<Tag> tags;

    {
        persons = FXCollections.observableArrayList(ExtractableObservables::extractFrom);
        tags = FXCollections.observableArrayList(ExtractableObservables::extractFrom);
    }

    public AddressBook() {}

    public AddressBook(AddressBook toBeCopied) {
        this(toBeCopied.getPersons(), toBeCopied.getTags());
    }

    public AddressBook(List<Person> persons, List<Tag> tags) {
        setPersons(persons);
        setTags(tags);
    }

    public ViewableAddressBook createVisibleAddressBook() {
        return new ViewableAddressBook(this);
    }

    @XmlElement(name = "persons")
    @JsonProperty("persons")
    public ObservableList<Person> getPersons() {
        return persons;
    }

    @XmlElement(name = "tags")
    @JsonProperty("tags")
    public ObservableList<Tag> getTags() {
        return tags;
    }

    public void clearData() {
        persons.clear();
        tags.clear();
    }

    public void resetData(Collection<Person> ps, Collection<Tag> ts) {
        persons.setAll(ps);
        tags.setAll(ts);
    }

    public void resetData(AddressBook newData) {
        resetData(newData.getPersons(), newData.getTags());
    }

    public void setPersons(List<Person> persons) {
        this.persons.setAll(persons);
    }

    public void setTags(List<Tag> tags) {
        this.tags.setAll(tags);
    }

    public Optional<Person> findPerson(ReadOnlyPerson personToFind) {
        for (Person p : persons) {
            if (p.equals(personToFind)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
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

    @Override
    public UnmodifiableObservableList<ReadOnlyPerson> getAllPersonsReadOnly() {
        return new UnmodifiableObservableList<>(persons);
    }

    @Override
    public UnmodifiableObservableList<Tag> getAllTagsReadOnly() {
        return new UnmodifiableObservableList<>(tags);
    }
}
