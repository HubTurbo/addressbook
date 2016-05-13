package address.model;

import address.events.EventManager;
import address.events.FilterCommittedEvent;
import address.events.LocalModelSyncedEvent;
import address.events.NewMirrorDataEvent;
import address.events.*;

import address.util.PlatformEx;
import com.google.common.eventbus.Subscribe;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.*;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager {

    private final ObservableList<Person> personData = FXCollections.observableArrayList();
    private final FilteredList<Person> filteredPersonData = new FilteredList<>(personData);
    private final ObservableList<ContactGroup> groupData = FXCollections.observableArrayList();

    /**
     * @param initialPersons Initial persons to populate the model.
     * @param initialGroups Initial groups to populate the model.
     */
    public ModelManager(List<Person> initialPersons, List<ContactGroup> initialGroups) {
        if (initialPersons == null || initialGroups == null) {
            appendSampleData();
        } else {
            System.out.println("Data found.");
            System.out.println("Persons found : " + initialPersons.size());
            personData.addAll(initialPersons);
            System.out.println("Groups found : " + initialGroups.size());
            groupData.addAll(initialGroups);
        }

        //Listen to any changed to person data and raise an event
        //Note: this will not catch edits to Person objects
        personData.addListener(
                (ListChangeListener<? super Person>) (change) ->
                        EventManager.getInstance().post(new LocalModelChangedEvent(personData, groupData)));

        //Listen to any changed to group data and raise an event
        //Note: this will not catch edits to ContactGroup objects
        groupData.addListener(
                (ListChangeListener<? super ContactGroup>) (change) ->
                        EventManager.getInstance().post(new LocalModelChangedEvent(personData, groupData)));

        //Register for general events relevant to data manager
        EventManager.getInstance().registerHandler(this);
    }

    public ModelManager(AddressBookWrapper addressBook) {
        this(addressBook == null ? null : addressBook.getPersons(),
            addressBook == null ? null : addressBook.getGroups());
    }

    public void appendSampleData() {
        final Person[] samplePersonData = {
            new Person("Hans", "Muster"),
            new Person("Ruth", "Mueller"),
            new Person("Heinz", "Kurz"),
            new Person("Cornelia", "Meier"),
            new Person("Werner", "Meyer"),
            new Person("Lydia", "Kunz"),
            new Person("Anna", "Best"),
            new Person("Stefan", "Meier"),
            new Person("Martin", "Mueller")
        };
        final ContactGroup[] sampleGroupData = {
            new ContactGroup("relatives"),
            new ContactGroup("friends")
        };
        personData.addAll(samplePersonData);
        groupData.addAll(sampleGroupData);
    }

    /**
     * @return observablelist of persons in model
     */
    public ObservableList<Person> getPersons() {
        return personData;
    }

    /**
     * @return data of persons in active filtered view
     */
    public ObservableList<Person> getFilteredPersons() {
        return filteredPersonData;
    }

    /**
     * @return observablelist of groups in model
     */
    public ObservableList<ContactGroup> getGroupData() {
        return groupData;
    }

    /**
     * Updates the details of a Person object. Updates to Person objects should be
     * done through this method to ensure the proper events are raised to indicate
     * a change to the model.
     * @param original The Person object to be changed.
     * @param updated The temporary Person object containing new values.
     */
    public synchronized void updatePerson(Person original, Person updated){
        assert !updated.getUpdatedAt().isBefore(original.getUpdatedAt());
        original.update(updated);
        EventManager.getInstance().post(new LocalModelChangedEvent(personData, groupData));
    }

    /**
     * Deletes the person from the model.
     * @param personToDelete
     */
    public synchronized void deletePerson(Person personToDelete){
        personData.remove(personToDelete);
    }

    /**
     * Adds a person to the model
     * @param personToAdd
     */
    public synchronized void addPerson(Person personToAdd) {
        personData.add(personToAdd);
    }

    /**
     * Updates the details of a ContactGroup object. Updates to ContactGroup objects should be
     * done through this method to ensure the proper events are raised to indicate
     * a change to the model.
     * @param original The ContactGroup object to be changed.
     * @param updated The temporary ContactGroup object containing new values.
     */
    public synchronized void updateGroup(ContactGroup original, ContactGroup updated){
        original.update(updated);
        EventManager.getInstance().post(new LocalModelChangedEvent(personData, groupData));
    }

    /**
     * Deletes the group from the model.
     * @param groupToDelete
     */
    public synchronized void deleteGroup(ContactGroup groupToDelete){
        groupData.remove(groupToDelete);
    }

    /**
     * Adds a group to the model
     * @param groupToAdd
     */
    public synchronized void addGroup(ContactGroup groupToAdd) {
        groupData.add(groupToAdd);
    }

    @Subscribe
    private synchronized void handleNewMirrorDataEvent(NewMirrorDataEvent nde){
        // NewMirrorDataEvent is created from outside FX Application thread
        PlatformEx.runLaterAndWait(() -> updateUsingExternalData(nde.data));
        EventManager.getInstance().post(new LocalModelSyncedEvent(personData, groupData));
    }

    @Subscribe
    private void handleFilterCommittedEvent(FilterCommittedEvent fce) {
        filteredPersonData.setPredicate(fce.filterExpression::satisfies);
    }

    /**
     * Diffs extData with the current model and updates the current model with minimal change.
     * @param extData data from an external canonical source
     */
    public void updateUsingExternalData(AddressBookWrapper extData) {
        assert !extData.containsDuplicates() : "Duplicates are not allowed.";
        diffUpdate(extData.getPersons(), personData);
        diffUpdate(extData.getGroups(), groupData);
    }

    /**
     * Performs a diff-update (minimal change) on target using newData.
     * Specification:
     *   _________________________________________________
     *  | in newData | in target | Result                |
     *  --------------------------------------------------
     *  | yes        | yes       | update item in target |
     *  | yes        | no        | remove from target    |
     *  | no         | yes       | copy-add to target    |
     *  | no         | no        | N/A                   |
     *  --------------------------------------------------
     * Any form of data element ordering in newData will not be enforced on target.
     *
     * @param newData
     * @param target
     * @param <E>
     */
    public static <E extends UniqueCopyable<E>> void diffUpdate(Collection<E> newData, Collection<E> target) {
        final Map<E, E> unconsidered = new HashMap<>();
        newData.forEach((item) -> unconsidered.put(item, item));

        final Iterator<E> targetIter = target.iterator();
        while (targetIter.hasNext()) {
            final E oldItem = targetIter.next();
            final E newItem = unconsidered.remove(oldItem);
            if (newItem == null) { // not in newData
                targetIter.remove();
            } else { // in newData
                oldItem.update(newItem);
            }
        }

        // not in target
        unconsidered.keySet().forEach((item) -> target.add(item));
    }

    /**
     * Clears existing model and replaces with the provided new data.
     * @param newPeople
     */
    public void resetData(List<Person> newPeople, List<ContactGroup> newGroups) {
        personData.setAll(newPeople);
        groupData.setAll(newGroups);
    }

    public void resetData(AddressBookWrapper newData) {
        resetData(newData.getPersons(), newData.getGroups());
    }
}
