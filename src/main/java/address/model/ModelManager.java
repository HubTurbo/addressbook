package address.model;

import address.events.*;

import address.exceptions.DuplicateDataException;
import address.exceptions.DuplicateTagException;
import address.exceptions.DuplicatePersonException;
import address.model.datatypes.*;
import address.model.datatypes.person.*;
import address.model.datatypes.tag.Tag;
import address.model.datatypes.UniqueData;
import address.util.AppLogger;
import address.util.LoggerManager;
import address.util.collections.UnmodifiableObservableList;
import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents the in-memory model of the address book data.
 * All changes to any model should be synchronized. (FX and sync thread may clash).
 */
public class ModelManager implements ReadOnlyAddressBook, ReadOnlyViewableAddressBook {
    private static final AppLogger logger = LoggerManager.getLogger(ModelManager.class);

    private final AddressBook backingModel;
    private final ViewableAddressBook visibleModel;
    private final ScheduledExecutorService scheduler;

    {
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Initializes a ModelManager with the given AddressBook
     * AddressBook and its variables should not be null.
     * @param src
     */
    public ModelManager(AddressBook src) {
        if (src == null) {
            logger.fatal("Attempted to initialize with a null AddressBook");
            assert false;
        }
        logger.debug("Initializing with address book: {}", src);

        backingModel = new AddressBook(src);
        visibleModel = backingModel.createVisibleAddressBook();

        // update changes need to go through #updatePerson or #updateTag to trigger the LMCEvent
        final ListChangeListener<Object> modelChangeListener = change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    EventManager.getInstance().post(new LocalModelChangedEvent(this));
                    return;
                }
            }
        };
        backingPersonList().addListener(modelChangeListener);
        backingTagList().addListener(modelChangeListener);

        EventManager.getInstance().registerHandler(this);
    }

    public ModelManager() {
        this(new AddressBook());
    }

    /**
     * Clears existing backing model and replaces with the provided new data.
     */
    public void resetData(AddressBook newData) {
        backingModel.resetData(newData);
    }

    public void clearModel() {
        backingModel.clearData();
    }

//// EXPOSING MODEL

    /**
     * @return all persons in visible model IN AN UNMODIFIABLE VIEW
     */
    @Override
    public UnmodifiableObservableList<ReadOnlyViewablePerson> getAllViewablePersonsReadOnly() {
        return visibleModel.getAllViewablePersonsReadOnly();
    }

    /**
     * @return all tags in backing model IN AN UNMODIFIABLE VIEW
     */
    @Override
    public UnmodifiableObservableList<Tag> getAllViewableTagsReadOnly() {
        return visibleModel.getAllViewableTagsReadOnly();
    }

    @Override
    public List<ReadOnlyPerson> getPersonList() {
        return backingModel.getPersonList();
    }

    @Override
    public List<Tag> getTagList() {
        return backingModel.getTagList();
    }

    @Override
    public UnmodifiableObservableList<ReadOnlyPerson> getPersonsAsReadOnlyObservableList() {
        return backingModel.getPersonsAsReadOnlyObservableList();
    }

    @Override
    public UnmodifiableObservableList<Tag> getTagsAsReadOnlyObservableList() {
        return backingModel.getTagsAsReadOnlyObservableList();
    }

    /**
     * @return persons list in backing model
     */
    public ObservableList<Person> backingPersonList() {
        return backingModel.getPersons();
    }

    /**
     * @return tags list in backing model
     */
    public ObservableList<Tag> backingTagList() {
        return backingModel.getTags();
    }


//// CREATE

    /**
     * Adds a person to the model
     * @throws DuplicatePersonException when this operation would cause duplicates
     */
    public synchronized void addPerson(ReadOnlyPerson data) throws DuplicatePersonException {
        Person toAdd;
        do { // make sure no id clashes.
            toAdd = new Person(Math.abs(UUID.randomUUID().hashCode()));
        } while (backingPersonList().contains(toAdd));
        toAdd.update(data);
        backingPersonList().add(toAdd);
    }

    /**
     * Adds a tag to the model
     * @param tagToAdd
     * @throws DuplicateTagException when this operation would cause duplicates
     */
    public synchronized void addTag(Tag tagToAdd) throws DuplicateTagException {
        if (backingTagList().contains(tagToAdd)) {
            throw new DuplicateTagException(tagToAdd);
        }
        backingTagList().add(tagToAdd);
    }

    /**
     * Adds multiple tags to the model as an atomic action (triggers only 1 ModelChangedEvent)
     * @param toAdd
     * @throws DuplicateDataException when this operation would cause duplicates
     */
    public synchronized void addTag(Collection<Tag> toAdd) throws DuplicateDataException {
        if (!UniqueData.canCombineWithoutDuplicates(backingTagList(), toAdd)) {
            throw new DuplicateDataException("Adding these " + toAdd.size() + " new tags");
        }
        backingTagList().addAll(toAdd);
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
    public synchronized void updatePerson(ReadOnlyPerson target, ReadOnlyPerson updatedData)
            throws DuplicatePersonException {
        if (!target.equals(updatedData) && backingPersonList().contains(updatedData)) {
            throw new DuplicatePersonException(updatedData);
        }

        backingModel.findPerson(target).get().update(updatedData);
        EventManager.getInstance().post(new LocalModelChangedEvent(this));
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
        if (!original.equals(updated) && backingTagList().contains(updated)) {
            throw new DuplicateTagException(updated);
        }
        original.update(updated);
        EventManager.getInstance().post(new LocalModelChangedEvent(this));
    }

//// DELETE

    /**
     * Deletes the person from the model.
     * @param personToDelete
     * @return true if there was a successful removal
     */
    public synchronized boolean deletePerson(Person personToDelete){
        return backingPersonList().remove(personToDelete);
    }


    public void delayedDeletePerson(ReadOnlyPerson toDelete, int delay, TimeUnit step) {
        final Optional<ViewablePerson> deleteTarget = visibleModel.findPerson(toDelete);
        assert deleteTarget.isPresent();
        deleteTarget.get().setIsDeleted(true);
        scheduler.schedule(()-> Platform.runLater(()->deletePerson(new Person(toDelete))), delay, step);
    }

    /**
     * Deletes multiple persons from the model as an atomic action (triggers only 1 ModelChangedEvent)
     * @param toDelete
     * @return true if there was at least one successful removal
     */
    public synchronized boolean deletePersons(Collection<? extends ReadOnlyPerson> toDelete) {
        return ReadOnlyPerson.removeAll(backingPersonList(), toDelete);
    }

    /**
     * Deletes the tag from the model.
     * @param tagToDelete
     * @return true if there was a successful removal
     */
    public synchronized boolean deleteTag(Tag tagToDelete) {
        return backingTagList().remove(tagToDelete);
    }

    /**
     * Deletes multiple persons from the model as an atomic action (triggers only 1 ModelChangedEvent)
     * @param toDelete
     * @return true if there was at least one successful removal
     */
    public synchronized boolean deleteTags(Collection<Tag> toDelete) {
        return backingTagList().removeAll(new HashSet<>(toDelete)); // O(1) .contains boosts performance
    }

//// EVENT HANDLERS

    @Subscribe
    private <T> void handleUpdateCompletedEvent(UpdateCompletedEvent<T> uce) {
        // Sync is done outside FX Application thread
        // TODO: Decide how incoming updates should be handled
        //PlatformEx.runLaterAndWait(() -> updateUsingExternalData(uce.getData()));
    }

//// DIFFERENTIAL UPDATE ENGINE todo shift this logic to sync component (with conditional requests to remote)

    /**
     * Diffs extData with the current model and updates the current model with minimal change.
     * @param extData data from an external canonical source
     */
    public synchronized void updateUsingExternalData(ReadOnlyAddressBook extData) {
        final AddressBook data = new AddressBook(extData);
        assert !data.containsDuplicates() : "Duplicates are not allowed in an AddressBook";
        boolean hasPersonsUpdates = diffUpdate(backingPersonList(), data.getPersons());
        boolean hasTagsUpdates = diffUpdate(backingTagList(), data.getTags());
        if (hasPersonsUpdates || hasTagsUpdates) {
            EventManager.getInstance().post(new LocalModelChangedEvent(this));
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
