package address.model.datatypes;

import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.model.datatypes.person.ViewablePerson;
import address.model.datatypes.tag.Tag;
import address.util.collections.UnmodifiableObservableList;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ViewableAddressBook implements ReadOnlyViewableAddressBook {

    private final AddressBook backingModel;

    private final ObservableList<ViewablePerson> persons;
    private final List<ViewablePerson> personBackingList;
    private final ObservableList<Tag> tags; // todo change to viewabletag class

    {
        personBackingList = new ArrayList<>();
        persons = FXCollections.observableList(personBackingList);
    }

    ViewableAddressBook(AddressBook src) {
        backingModel = src;
        tags = backingModel.getTags(); // change when viewabletag is implemented

        persons.setAll(backingModel.getPersons().stream()
                .map(ViewablePerson::fromBacking)
                .collect(Collectors.toList()));

        bindViewablePersonListToBackingList();
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private void bindViewablePersonListToBackingList() {
        backingModel.getPersons().addListener((ListChangeListener<? super Person>) change -> {

            // ignore permutations (order doesn't matter) and updates (ViewableDataType wrapper handles it)
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    // removed
                    ReadOnlyPerson.removeAllWithSameIds(persons, change.getRemoved());
                    // newly added
                    persons.addAll(change.getAddedSubList().stream()
                            .map(ViewablePerson::fromBacking)
                            .collect(Collectors.toList()));
                }
            }

        });
    }

//// person-level operations

    public boolean containsPerson(ReadOnlyPerson key) {
        return ReadOnlyPerson.containsById(persons, key);
    }

    public boolean containsPerson(int id) {
        return ReadOnlyPerson.containsById(persons, id);
    }

    public Optional<ViewablePerson> findPerson(ReadOnlyPerson key) {
        return ReadOnlyPerson.findById(persons, key);
    }

    public Optional<ViewablePerson> findPerson(int id) {
        return ReadOnlyPerson.findById(persons, id);
    }

    public void addPerson(ViewablePerson p){
        persons.add(p);
    }

    /**
     * Does not trigger any listeners on the person observablelist
     */
    public void addPersonSilently(ViewablePerson p) {
        personBackingList.add(p);
    }

    public boolean removePerson(ReadOnlyPerson key) {
        return ReadOnlyPerson.removeOneById(persons, key);
    }

    public boolean removePerson(int id) {
        return ReadOnlyPerson.removeOneById(persons, id);
    }

    /**
     * Does not trigger any listeners on the person observablelist
     */
    public boolean removePersonSilently(ReadOnlyPerson key) {
        return ReadOnlyPerson.removeOneById(personBackingList, key);
    }

    /**
     * Does not trigger any listeners on the person observablelist
     */
    public boolean removePersonSilently(int id) {
        return ReadOnlyPerson.removeOneById(personBackingList, id);
    }

//// .

    public ObservableList<ViewablePerson> getPersons() {
        return persons;
    }

    public ObservableList<Tag> getTags() {
        return tags;
    }

    @Override
    public UnmodifiableObservableList<ReadOnlyViewablePerson> getAllViewablePersonsReadOnly() {
        return new UnmodifiableObservableList<>(persons);
    }

    @Override
    public UnmodifiableObservableList<Tag> getAllViewableTagsReadOnly() {
        return new UnmodifiableObservableList<>(tags);
    }

    @Override
    public List<ReadOnlyPerson> getPersonList() {
        return Collections.unmodifiableList(persons);
    }

    @Override
    public List<Tag> getTagList() {
        return Collections.unmodifiableList(tags);
    }

    @Override
    public UnmodifiableObservableList<ReadOnlyPerson> getPersonsAsReadOnlyObservableList() {
        return new UnmodifiableObservableList<>(persons);
    }

    @Override
    public UnmodifiableObservableList<Tag> getTagsAsReadOnlyObservableList() {
        return new UnmodifiableObservableList<>(tags);
    }

}
