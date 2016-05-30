package address.model;

import address.events.*;

import address.exceptions.DuplicateDataException;
import address.exceptions.DuplicateGroupException;
import address.exceptions.DuplicatePersonException;
import address.model.datatypes.ContactGroup;
import address.model.datatypes.Person;
import address.model.datatypes.UniqueData;
import address.util.PlatformEx;
import com.google.common.eventbus.Subscribe;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Represents the in-memory model of the address book data.
 * All changes to model should be synchronized.
 */
public class ModelManager {

    private final ObservableList<Person> personModel = FXCollections.observableArrayList();
    private final FilteredList<Person> filteredPersonModel = new FilteredList<>(personModel);
    private final ObservableList<ContactGroup> groupModel = FXCollections.observableArrayList();

    /**
     * @param initialPersons Initial persons to populate the model.
     * @param initialGroups Initial groups to populate the model.
     */
    public ModelManager(List<Person> initialPersons, List<ContactGroup> initialGroups) {
        System.out.println("Data found.");
        System.out.println("Persons found : " + initialPersons.size());
        System.out.println("Groups found : " + initialGroups.size());

        resetData(initialPersons, initialGroups);

        //Listen to any changed to person data and raise an event
        //Note: this will not catch edits to Person objects
        personModel.addListener(
                (ListChangeListener<? super Person>) (change) ->
                        EventManager.getInstance().post(new LocalModelChangedEvent(personModel, groupModel)));

        //Listen to any changed to group data and raise an event
        //Note: this will not catch edits to ContactGroup objects
        groupModel.addListener(
                (ListChangeListener<? super ContactGroup>) (change) ->
                        EventManager.getInstance().post(new LocalModelChangedEvent(personModel, groupModel)));

        //Register for general events relevant to data manager
        EventManager.getInstance().registerHandler(this);
    }

    public ModelManager(AddressBook addressBook) {
        this(addressBook.getPersons(), addressBook.getGroups());
    }

    public ModelManager() {
        this(new AddressBook());
    }

    public synchronized void resetWithSampleData() throws DuplicateDataException {
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
        resetData(Arrays.asList(samplePersonData), Arrays.asList(sampleGroupData));
    }

    /**
     * Clears existing model and replaces with the provided new data. Selection is lost.
     * @param newPeople
     */
    public synchronized void resetData(List<Person> newPeople, List<ContactGroup> newGroups) {
        personModel.setAll(newPeople);
        groupModel.setAll(newGroups);
    }

    public void resetData(AddressBook newData) {
        resetData(newData.getPersons(), newData.getGroups());
    }

    public void clearModel() {
        resetData(Collections.emptyList(), Collections.emptyList());
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
        if (personModel.contains(personToAdd)) {
            throw new DuplicatePersonException(personToAdd);
        }
        personModel.add(personToAdd);
    }

    /**
     * Adds multiple persons to the model as an atomic action (triggers only 1 ModelChangedEvent)
     * @param toAdd
     * @throws DuplicateDataException when this operation would cause duplicates
     */
    public synchronized void addPersons(Collection<Person> toAdd) throws DuplicateDataException {
        if (!UniqueData.canCombineWithoutDuplicates(personModel, toAdd)) {
            throw new DuplicateDataException("Adding these " + toAdd.size() + " new people");
        }
        personModel.addAll(toAdd);
    }

    /**
     * Adds a group to the model
     * @param groupToAdd
     * @throws DuplicateGroupException when this operation would cause duplicates
     */
    public synchronized void addGroup(ContactGroup groupToAdd) throws DuplicateGroupException {
        if (groupModel.contains(groupToAdd)) {
            throw new DuplicateGroupException(groupToAdd);
        }
        groupModel.add(groupToAdd);
    }

    /**
     * Adds multiple groups to the model as an atomic action (triggers only 1 ModelChangedEvent)
     * @param toAdd
     * @throws DuplicateDataException when this operation would cause duplicates
     */
    public synchronized void addGroups(Collection<ContactGroup> toAdd) throws DuplicateDataException {
        if (!UniqueData.canCombineWithoutDuplicates(groupModel, toAdd)) {
            throw new DuplicateDataException("Adding these " + toAdd.size() + " new contact groups");
        }
        groupModel.addAll(toAdd);
    }

///////////////////////////////////////////////////////////////////////
// READ
///////////////////////////////////////////////////////////////////////

    /**
     * @return all persons in model
     */
    public ObservableList<Person> getPersonsModel() {
        return personModel;
    }

    /**
     * @return persons in active filtered view
     */
    public ObservableList<Person> getFilteredPersons() {
        return filteredPersonModel;
    }

    /**
     * @return all groups in model
     */
    public ObservableList<ContactGroup> getGroupModel() {
        return groupModel;
    }

    public List<ContactGroup> getGroups() {
        return groupModel.stream()
                .map(group -> (ContactGroup) group)
                .collect(Collectors.toList());
    }

    public List<Person> getPersons() {
        return personModel.stream()
                .map(person -> (Person) person)
                .collect(Collectors.toList());
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
        if (!(new Person(original)).equals(updated)
                && !personModel.stream().map(Person::new).noneMatch(updated::equals)) {
            throw new DuplicatePersonException(updated);
        }
        original.update(updated);
        EventManager.getInstance().post(new LocalModelChangedEvent(personModel, groupModel));
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
        if (!(new ContactGroup(original)).equals(updated)
                && !groupModel.stream().map(ContactGroup::new).noneMatch(updated::equals)) {
            throw new DuplicateGroupException(updated);
        }
        original.update(updated);
        EventManager.getInstance().post(new LocalModelChangedEvent(personModel, groupModel));
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
        return personModel.remove(personToDelete);
    }

    /**
     * Deletes multiple persons from the model as an atomic action (triggers only 1 ModelChangedEvent)
     * @param toDelete
     * @return true if there was at least one successful removal
     */
    public synchronized boolean deletePersons(Collection<Person> toDelete) {
        return personModel.removeAll(new HashSet<>(toDelete)); // O(1) .contains boosts performance
    }

    /**
     * Deletes the group from the model.
     * @param groupToDelete
     * @return true if there was a successful removal
     */
    public synchronized boolean deleteGroup(ContactGroup groupToDelete){
        return groupModel.remove(groupToDelete);
    }

    /**
     * Deletes multiple persons from the model as an atomic action (triggers only 1 ModelChangedEvent)
     * @param toDelete
     * @return true if there was at least one successful removal
     */
    public synchronized boolean deleteGroups(Collection<ContactGroup> toDelete) {
        return groupModel.removeAll(new HashSet<>(toDelete)); // O(1) .contains boosts performance
    }

///////////////////////////////////////////////////////////////////////
// EVENT HANDLERS
///////////////////////////////////////////////////////////////////////

    @Subscribe
    private void handleFilterCommittedEvent(FilterCommittedEvent fce) {
        filteredPersonModel.setPredicate(fce.filterExpression::satisfies);
    }

    @Subscribe
    private void handleNewMirrorDataEvent(NewMirrorDataEvent nde){
        // NewMirrorDataEvent is created from outside FX Application thread
        PlatformEx.runLaterAndWait(() -> updateUsingExternalData(nde.data));
        EventManager.getInstance().post(new LocalModelSyncedFromCloudEvent(personModel, groupModel));
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
        if (diffUpdate(personModel, extData.getPersons()) || diffUpdate(groupModel, extData.getGroups())) {
            EventManager.getInstance().post(new LocalModelChangedEvent(personModel, groupModel));
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
    private synchronized <E extends UniqueData> boolean diffUpdate(Collection<E> target, Collection<E> newData) {
        assert UniqueData.itemsAreUnique(target) : "target of diffUpdate should not have duplicates";
        assert UniqueData.itemsAreUnique(newData) : "newData for diffUpdate should not have duplicates";

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
     * Allows generic UniqueData .update() calling without having to know which class it is.
     * Because java does not allow self-referential generic type parameters.
     *
     * Does not trigger any events.
     *
     * @param target to be updated
     * @param newData data used for update
     */
    private <E extends UniqueData> void updateDataItem(E target, E newData) {
        if (target instanceof Person && newData instanceof Person) {
            ((Person) target).update((Person) newData);
            return;
        }
        if (target instanceof ContactGroup && newData instanceof ContactGroup) {
            ((ContactGroup) target).update((ContactGroup) newData);
            return;
        }
        assert false : "need to add logic for any new UniqueData classes";
    }
}
