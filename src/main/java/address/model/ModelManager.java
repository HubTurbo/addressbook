package address.model;

import address.events.*;

import address.exceptions.DuplicateDataException;
import address.exceptions.DuplicateTagException;
import address.exceptions.DuplicatePersonException;
import address.model.datatypes.Person;
import address.model.datatypes.Tag;
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
    private final ObservableList<Tag> tagModel = FXCollections.observableArrayList();

    /**
     * @param initialPersons Initial persons to populate the model.
     * @param initialTags Initial tags to populate the model.
     */
    public ModelManager(List<Person> initialPersons, List<Tag> initialTags) {
        System.out.println("Data found.");
        System.out.println("Persons found : " + initialPersons.size());
        System.out.println("Tags found : " + initialTags.size());

        resetData(initialPersons, initialTags);

        //Listen to any changed to person data and raise an event
        //Note: this will not catch edits to Person objects
        personModel.addListener(
                (ListChangeListener<? super Person>) (change) ->
                        EventManager.getInstance().post(new LocalModelChangedEvent(personModel, tagModel)));

        //Listen to any changed to tag data and raise an event
        //Note: this will not catch edits to Tag objects
        tagModel.addListener(
                (ListChangeListener<? super Tag>) (change) ->
                        EventManager.getInstance().post(new LocalModelChangedEvent(personModel, tagModel)));

        //Register for general events relevant to data manager
        EventManager.getInstance().registerHandler(this);
    }

    public ModelManager(AddressBook addressBook) {
        this(addressBook.getPersons(), addressBook.getTags());
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
        final Tag[] sampleTagData = {
            new Tag("relatives"),
            new Tag("friends")
        };
        resetData(Arrays.asList(samplePersonData), Arrays.asList(sampleTagData));
    }

    /**
     * Clears existing model and replaces with the provided new data. Selection is lost.
     * @param newPeople
     */
    public synchronized void resetData(List<Person> newPeople, List<Tag> newTags) {
        personModel.setAll(newPeople);
        tagModel.setAll(newTags);
    }

    public void resetData(AddressBook newData) {
        resetData(newData.getPersons(), newData.getTags());
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
     * Adds a tag to the model
     * @param tagToAdd
     * @throws DuplicateTagException when this operation would cause duplicates
     */
    public synchronized void addTag(Tag tagToAdd) throws DuplicateTagException {
        if (tagModel.contains(tagToAdd)) {
            throw new DuplicateTagException(tagToAdd);
        }
        tagModel.add(tagToAdd);
    }

    /**
     * Adds multiple tags to the model as an atomic action (triggers only 1 ModelChangedEvent)
     * @param toAdd
     * @throws DuplicateDataException when this operation would cause duplicates
     */
    public synchronized void addTag(Collection<Tag> toAdd) throws DuplicateDataException {
        if (!UniqueData.canCombineWithoutDuplicates(tagModel, toAdd)) {
            throw new DuplicateDataException("Adding these " + toAdd.size() + " new tags");
        }
        tagModel.addAll(toAdd);
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
    public ObservableList<Tag> getTagModel() {
        return tagModel;
    }

    public List<Tag> getTags() {
        return tagModel.stream()
                .map(tag -> (Tag) tag)
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
        EventManager.getInstance().post(new LocalModelChangedEvent(personModel, tagModel));
    }

    /**
     * Updates the details of a Tag object. Updates to Tag objects should be
     * done through this method to ensure the proper events are raised to indicate
     * a change to the model. TODO listen on Tag properties and not manually raise events here.
     *
     * @param original The Tag object to be changed.
     * @param updated The temporary Tag object containing new values.
     */
    public synchronized void updateTag(Tag original, Tag updated) throws DuplicateTagException {
        if (!(new Tag(original)).equals(updated)
                && !tagModel.stream().map(Tag::new).noneMatch(updated::equals)) {
            throw new DuplicateTagException(updated);
        }
        original.update(updated);
        EventManager.getInstance().post(new LocalModelChangedEvent(personModel, tagModel));
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
     * Deletes the tag from the model.
     * @param tagToDelete
     * @return true if there was a successful removal
     */
    public synchronized boolean deleteTag(Tag tagToDelete){
        return tagModel.remove(tagToDelete);
    }

    /**
     * Deletes multiple persons from the model as an atomic action (triggers only 1 ModelChangedEvent)
     * @param toDelete
     * @return true if there was at least one successful removal
     */
    public synchronized boolean deleteTags(Collection<Tag> toDelete) {
        return tagModel.removeAll(new HashSet<>(toDelete)); // O(1) .contains boosts performance
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
        EventManager.getInstance().post(new LocalModelSyncedFromCloudEvent(personModel, tagModel));
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
        if (diffUpdate(personModel, extData.getPersons()) || diffUpdate(tagModel, extData.getTags())) {
            EventManager.getInstance().post(new LocalModelChangedEvent(personModel, tagModel));
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
        if (target instanceof Tag && newData instanceof Tag) {
            ((Tag) target).update((Tag) newData);
            return;
        }
        assert false : "need to add logic for any new UniqueData classes";
    }
}
