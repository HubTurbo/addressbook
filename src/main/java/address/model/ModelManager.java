package address.model;

import address.events.*;

import address.exceptions.DuplicateDataException;
import address.exceptions.DuplicateTagException;
import address.exceptions.DuplicatePersonException;
import address.model.datatypes.*;
import address.util.PlatformEx;
import com.google.common.eventbus.Subscribe;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Represents the in-memory model of the address book data.
 * All changes to any model should be synchronized. (FX and sync thread may clash).
 */
public class ModelManager implements Model, VisibleModel {

    private final BackingAddressBook backingModel;
    private final VisibleAddressBook visibleModel;

    public ModelManager(AddressBook src) {
        System.out.println("Data found.");
        System.out.println("Persons found : " + src.getPersons().size());
        System.out.println("Tags found : " + src.getTags().size());

        backingModel = new BackingAddressBook(src);
        visibleModel = backingModel.createVisibleAddressBook();

        // update changes need to go through #updatePerson or #updateTag to trigger the LMCEvent
        final ListChangeListener<BaseDataType> modelChangeListener = change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    EventManager.getInstance().post(new LocalModelChangedEvent(getAllPersons(), getAllTags()));
                    return;
                }
            }
        };
        getAllPersons().addListener(modelChangeListener);
        getAllTags().addListener(modelChangeListener);

        EventManager.getInstance().registerHandler(this);
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
        resetData(new AddressBook(Arrays.asList(samplePersonData), Arrays.asList(sampleTagData)));
    }

    /**
     * Clears existing backing model and replaces with the provided new data.
     */
    public void resetData(AddressBook newData) {
        backingModel.resetData(newData);
    }

    public void clearModel() {
        backingModel.clearModel();
    }

//// EXPOSING MODEL

    /**
     * @return all persons in visible model IN AN UNMODIFIABLE VIEW
     */
    @Override
    public ObservableList<ObservableViewablePerson> getAllViewablePersonsAsObservable() {
        return visibleModel.getAllViewablePersonsAsObservable();
    }

    /**
     * @return all persons in visible model IN AN UNMODIFIABLE VIEW
     */
    @Override
    public ObservableList<ReadableViewablePerson> getAllViewablePersonsAsReadOnly() {
        return visibleModel.getAllViewablePersonsAsReadOnly();
    }

    /**
     * @return all groups in visible model IN AN UNMODIFIABLE VIEW
     */
    @Override
    public ObservableList<Tag> getAllViewableTags() {
        return visibleModel.getAllViewableTags();
    }

    /**
     * @return all persons in backing model
     */
    @Override
    public ObservableList<Person> getAllPersons() {
        return backingModel.getAllPersons();
    }

    /**
     * @return all tags in backing model
     */
    @Override
    public ObservableList<Tag> getAllTags() {
        return backingModel.getAllTags();
    }


//// CREATE

    /**
     * Adds a person to the model
     * @throws DuplicatePersonException when this operation would cause duplicates
     */
    public synchronized void addPerson(ReadablePerson personToAdd) throws DuplicatePersonException {
        final Person toAdd = new Person(personToAdd);
        if (getAllPersons().contains(toAdd)) {
            throw new DuplicatePersonException(personToAdd);
        }
        getAllPersons().add(toAdd);
    }

    /**
     * Adds multiple persons to the model as an atomic action (triggers only 1 ModelChangedEvent)
     * @param toAdd
     * @throws DuplicateDataException when this operation would cause duplicates
     */
    public synchronized void addPersons(Collection<Person> toAdd) throws DuplicateDataException {
        if (!UniqueData.canCombineWithoutDuplicates(getAllPersons(), toAdd)) {
            throw new DuplicateDataException("Adding these " + toAdd.size() + " new people");
        }
        getAllPersons().addAll(toAdd);
    }

    /**
     * Adds a tag to the model
     * @param tagToAdd
     * @throws DuplicateTagException when this operation would cause duplicates
     */
    public synchronized void addTag(Tag tagToAdd) throws DuplicateTagException {
        if (getAllTags().contains(tagToAdd)) {
            throw new DuplicateTagException(tagToAdd);
        }
        getAllTags().add(tagToAdd);
    }

    /**
     * Adds multiple tags to the model as an atomic action (triggers only 1 ModelChangedEvent)
     * @param toAdd
     * @throws DuplicateDataException when this operation would cause duplicates
     */
    public synchronized void addTag(Collection<Tag> toAdd) throws DuplicateDataException {
        if (!UniqueData.canCombineWithoutDuplicates(getAllTags(), toAdd)) {
            throw new DuplicateDataException("Adding these " + toAdd.size() + " new tags");
        }
        getAllTags().addAll(toAdd);
    }

//// READ

    // todo

//// UPDATE

    /**
     * Updates the details of a Person object. Updates to Person objects should be
     * done through this method to ensure the proper events are raised to indicate
     * a change to the model. TODO listen on Person properties and not manually raise events here.
     * @param target The Person object to be changed.
     * @param updatedData The temporary Person object containing new values.
     */
    public synchronized void updatePerson(ReadablePerson target, ReadablePerson updatedData) throws DuplicatePersonException {
        if (!target.equals(updatedData) && getAllPersons().contains(updatedData)) {
            throw new DuplicatePersonException(updatedData);
        }

        backingModel.findPerson(target).get().update(updatedData);
        EventManager.getInstance().post(new LocalModelChangedEvent(getAllPersons(), getAllTags()));
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
        if (!original.equals(updated) && getAllTags().contains(updated)) {
            throw new DuplicateTagException(updated);
        }
        original.update(updated);
        EventManager.getInstance().post(new LocalModelChangedEvent(getAllPersons(), getAllTags()));
    }

//// DELETE

    /**
     * Deletes the person from the model.
     * @param personToDelete
     * @return true if there was a successful removal
     */
    public synchronized boolean deletePerson(ReadablePerson personToDelete){
        return getAllPersons().remove(personToDelete);
    }

    /**
     * Deletes multiple persons from the model as an atomic action (triggers only 1 ModelChangedEvent)
     * @param toDelete
     * @return true if there was at least one successful removal
     */
    public synchronized boolean deletePersons(Collection<? extends ReadablePerson> toDelete) {
        return getAllPersons().removeAll(new HashSet<>(toDelete));
    }

    /**
     * Deletes the tag from the model.
     * @param tagToDelete
     * @return true if there was a successful removal
     */
    public synchronized boolean deleteTag(Tag tagToDelete){
        return getAllTags().remove(tagToDelete);
    }

    /**
     * Deletes multiple persons from the model as an atomic action (triggers only 1 ModelChangedEvent)
     * @param toDelete
     * @return true if there was at least one successful removal
     */
    public synchronized boolean deleteTags(Collection<Tag> toDelete) {
        return getAllTags().removeAll(new HashSet<>(toDelete)); // O(1) .contains boosts performance
    }

//// EVENT HANDLERS

    @Subscribe
    private void handleNewMirrorDataEvent(NewMirrorDataEvent nde){
        // NewMirrorDataEvent is created from outside FX Application thread
        PlatformEx.runLaterAndWait(() -> updateUsingExternalData(nde.data));
    }

//// DIFFERENTIAL UPDATE ENGINE todo shift this logic to sync component (with conditional requests to remote)

    /**
     * Diffs extData with the current model and updates the current model with minimal change.
     * @param extData data from an external canonical source
     */
    public synchronized void updateUsingExternalData(AddressBook extData) {
        assert !extData.containsDuplicates() : "Duplicates are not allowed in an AddressBook";
        if (diffUpdate(getAllPersons(), extData.getPersons()) || diffUpdate(getAllTags(), extData.getTags())) {
            EventManager.getInstance().post(new LocalModelChangedEvent(getAllPersons(), getAllTags()));
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

        final Map<E, E> remaining = new HashMap<>(); // has to be map; sets do not allow elemental retrieval
        newData.forEach((item) -> remaining.put(item, item));

        final Set<E> toBeRemoved = new HashSet<>();
        final AtomicBoolean changed = new AtomicBoolean(false);

        // handle updates to existing data objects
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
