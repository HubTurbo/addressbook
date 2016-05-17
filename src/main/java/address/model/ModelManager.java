package address.model;

import address.events.EventManager;
import address.events.FilterCommittedEvent;
import address.events.LocalModelSyncedFromCloudEvent;
import address.events.NewMirrorDataEvent;
import address.events.*;

import address.exceptions.DuplicateDataException;
import address.exceptions.DuplicateGroupException;
import address.exceptions.DuplicatePersonException;
import address.util.DataConstraints;
import address.util.PlatformEx;
import com.google.common.eventbus.Subscribe;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents the in-memory model of the address book data.
 * All changes to model should be synchronized.
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

    public ModelManager(AddressBook addressBook) {
        this(addressBook == null ? null : addressBook.getPersons(),
            addressBook == null ? null : addressBook.getGroups());
    }

    public synchronized void appendSampleData() {
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
     * Clears existing model and replaces with the provided new data. Selection is lost.
     * @param newPeople
     */
    public synchronized void resetData(List<Person> newPeople, List<ContactGroup> newGroups) {
        personData.setAll(newPeople);
        groupData.setAll(newGroups);
    }

    public void resetData(AddressBook newData) {
        resetData(newData.getPersons(), newData.getGroups());
    }

    ///////////////////////////////////////////////////////////////////////
    // CREATE
    ///////////////////////////////////////////////////////////////////////

    /**
     * Adds a person to the model
     * @param personToAdd
     * @throws DuplicatePersonException when this operation would cause duplicates
     */
    public synchronized void addPerson(Person personToAdd) throws DuplicatePersonException {
        if (personData.contains(personToAdd)) {
            throw new DuplicatePersonException(personToAdd);
        }
        personData.add(personToAdd);
    }

    /**
     * Adds multiple persons to the model as an atomic action (triggers only 1 ModelChangedEvent)
     * @param toAdd
     * @throws DuplicateDataException when this operation would cause duplicates
     */
    public synchronized void addPersons(Collection<Person> toAdd) throws DuplicateDataException {
        if (!DataConstraints.canCombineWithoutDuplicates(personData, toAdd)) {
            throw new DuplicateDataException("Adding these " + toAdd.size() + " new people");
        }
        personData.addAll(toAdd);
    }

    /**
     * Adds a group to the model
     * @param groupToAdd
     * @throws DuplicateGroupException when this operation would cause duplicates
     */
    public synchronized void addGroup(ContactGroup groupToAdd) throws DuplicateGroupException {
        if (groupData.contains(groupToAdd)) {
            throw new DuplicateGroupException(groupToAdd);
        }
        groupData.add(groupToAdd);
    }

    /**
     * Adds multiple groups to the model as an atomic action (triggers only 1 ModelChangedEvent)
     * @param toAdd
     * @throws DuplicateDataException when this operation would cause duplicates
     */
    public synchronized void addGroups(Collection<ContactGroup> toAdd) throws DuplicateDataException {
        if (!DataConstraints.canCombineWithoutDuplicates(groupData, toAdd)) {
            throw new DuplicateDataException("Adding these " + toAdd.size() + " new contact groups");
        }
        groupData.addAll(toAdd);
    }

    ///////////////////////////////////////////////////////////////////////
    // READ
    ///////////////////////////////////////////////////////////////////////

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

    ///////////////////////////////////////////////////////////////////////
    // UPDATE
    ///////////////////////////////////////////////////////////////////////

    /**
     * Updates the details of a Person object. Updates to Person objects should be
     * done through this method to ensure the proper events are raised to indicate
     * a change to the model. TODO listen on Person properties and not manually raise events here.
     * @param original The Person object to be changed.
     * @param updated The temporary Person object containing new values.
     */
    public synchronized void updatePerson(Person original, Person updated) throws DuplicatePersonException {
        if (!original.equals(updated) && personData.contains(updated)) {
            throw new DuplicatePersonException(updated);
        }
        original.update(updated);
        EventManager.getInstance().post(new LocalModelChangedEvent(personData, groupData));
    }

    /**
     * Updates the details of a ContactGroup object. Updates to ContactGroup objects should be
     * done through this method to ensure the proper events are raised to indicate
     * a change to the model. TODO listen on ContactGroup properties and not manually raise events here.
     *
     * @param original The ContactGroup object to be changed.
     * @param updated The temporary ContactGroup object containing new values.
     */
    public synchronized void updateGroup(ContactGroup original, ContactGroup updated) throws DuplicateGroupException {
        if (!original.equals(updated) && groupData.contains(updated)) {
            throw new DuplicateGroupException(updated);
        }
        original.update(updated);
        EventManager.getInstance().post(new LocalModelChangedEvent(personData, groupData));
    }

    ///////////////////////////////////////////////////////////////////////
    // DELETE
    ///////////////////////////////////////////////////////////////////////

    /**
     * Deletes the person from the model.
     * @param personToDelete
     * @return true if there was a successful removal
     */
    public synchronized boolean deletePerson(Person personToDelete){
        return personData.remove(personToDelete);
    }

    /**
     * Deletes multiple persons from the model as an atomic action (triggers only 1 ModelChangedEvent)
     * @param toDelete
     * @return true if there was at least one successful removal
     */
    public synchronized boolean deletePersons(Collection<Person> toDelete) {
        return personData.removeAll(new HashSet<>(toDelete)); // O(1) .contains boosts performance
    }

    /**
     * Deletes the group from the model.
     * @param groupToDelete
     * @return true if there was a successful removal
     */
    public synchronized boolean deleteGroup(ContactGroup groupToDelete){
        return groupData.remove(groupToDelete);
    }

    /**
     * Deletes multiple persons from the model as an atomic action (triggers only 1 ModelChangedEvent)
     * @param toDelete
     * @return true if there was at least one successful removal
     */
    public synchronized boolean deleteGroups(Collection<ContactGroup> toDelete) {
        return groupData.removeAll(new HashSet<>(toDelete)); // O(1) .contains boosts performance
    }

    ///////////////////////////////////////////////////////////////////////
    // EVENT HANDLERS
    ///////////////////////////////////////////////////////////////////////

    @Subscribe
    private void handleFilterCommittedEvent(FilterCommittedEvent fce) {
        filteredPersonData.setPredicate(fce.filterExpression::satisfies);
    }

    @Subscribe
    private void handleNewMirrorDataEvent(NewMirrorDataEvent nde){
        // NewMirrorDataEvent is created from outside FX Application thread
        PlatformEx.runLaterAndWait(() -> updateUsingExternalData(nde.data));
        EventManager.getInstance().post(new LocalModelSyncedFromCloudEvent(personData, groupData));
    }

    ///////////////////////////////////////////////////////////////////////
    // DIFFERENTIAL UPDATE ENGINE
    ///////////////////////////////////////////////////////////////////////

    /**
     * Diffs extData with the current model and updates the current model with minimal change.
     * @param extData data from an external canonical source
     */
    public synchronized void updateUsingExternalData(AddressBook extData) {
        assert !extData.containsDuplicates() : "Duplicates are not allowed in an AddressBook";
        boolean changed = false;
        changed = diffUpdate(personData, extData.getPersons());
        changed = changed || diffUpdate(groupData, extData.getGroups());
        if (changed) {
            EventManager.getInstance().post(new LocalModelChangedEvent(personData, groupData));
        }
    }

    /**
     * Performs a diff-update (minimal change) on target using newData.
     * Arguments newData and target should contain no duplicates.
     *
     * Does NOT trigger any events.
     *
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
     * @param target collection of data items to be updated
     * @param newData target will be updated to match newData's state
     * @return true if there were changes from the update.
     */
    private synchronized <E extends DataType> boolean diffUpdate(Collection<E> target, Collection<E> newData) {
        assert DataConstraints.itemsAreUnique(target) : "target of diffUpdate should not have duplicates";
        assert DataConstraints.itemsAreUnique(newData) : "newData for diffUpdate should not have duplicates";

        final Map<E, E> remaining = new HashMap<>(); // has to be map; sets do not allow specific retrieval
        newData.forEach((item) -> remaining.put(item, item));

        final Set<E> toBeRemoved = new HashSet<>();
        final AtomicBoolean changed = new AtomicBoolean(false);
        target.forEach(oldItem -> {
            final E newItem = remaining.remove(oldItem); // find matching item in unconsidered new data
            if (newItem == null) { // not in newData
                toBeRemoved.add(oldItem);
            } else { // exists in both new and old, update.
                updateDataItem(oldItem, newItem); // updates the items in target (reference points back to target)
                changed.set(true);
            }
        });
        final Set<E> toBeAdded = remaining.keySet();

        // .removeAll time complexity: O(n * complexity of argument's .contains call). Use a HashSet for O(n) time.
        target.removeAll(toBeRemoved);
        target.addAll(toBeAdded);

        return changed.get() || toBeAdded.size() > 0 || toBeRemoved.size() > 0;
    }

    /**
     * Allows generic DataType .update() calling without having to know which class it is.
     * Because java does not allow self-referential generic type parameters.
     *
     * Does not trigger any events.
     *
     * @param target to be updated
     * @param newData data used for update
     */
    private <E extends DataType> void updateDataItem(E target, E newData) {
        if (target instanceof Person && newData instanceof Person) {
            ((Person) target).update((Person) newData);
            return;
        }
        if (target instanceof ContactGroup && newData instanceof ContactGroup) {
            ((ContactGroup) target).update((ContactGroup) newData);
            return;
        }
        assert false : "need to add logic for any new DataType classes";
    }
}
