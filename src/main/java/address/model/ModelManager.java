package address.model;

import address.events.*;

import address.exceptions.DuplicateTagException;
import address.main.ComponentManager;
import address.model.datatypes.*;
import address.model.datatypes.person.*;
import address.model.datatypes.tag.Tag;
import address.model.datatypes.UniqueData;
import address.util.AppLogger;
import address.util.LoggerManager;
import address.util.PlatformExecUtil;
import address.util.collections.UnmodifiableObservableList;
import com.google.common.eventbus.Subscribe;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Represents the in-memory model of the address book data.
 * All changes to any model should be synchronized.
 */
public class ModelManager extends ComponentManager implements ReadOnlyAddressBook, ReadOnlyViewableAddressBook {
    private static final AppLogger logger = LoggerManager.getLogger(ModelManager.class);

    private final AddressBook backingModel;
    private final ViewableAddressBook visibleModel;
    private final Map<Integer, ChangePersonInModelCommand> personChangesInProgress;

    final Executor commandExecutor;

    private UserPrefs prefs;

    {
        personChangesInProgress = new HashMap<>();
        commandExecutor = Executors.newCachedThreadPool();
    }

    /**
     * Initializes a ModelManager with the given AddressBook
     * AddressBook and its variables should not be null
     */
    public ModelManager(AddressBook src, UserPrefs prefs) {
        super();
        if (src == null) {
            logger.fatal("Attempted to initialize with a null AddressBook");
            assert false;
        }
        logger.debug("Initializing with address book: {}", src);

        backingModel = new AddressBook(src);
        visibleModel = backingModel.createVisibleAddressBook();

        // update changes need to go through #editPersonThroughUI or #updateTag to trigger the LMCEvent
        final ListChangeListener<Object> modelChangeListener = change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    raise(new LocalModelChangedEvent(this));
                    return;
                }
            }
        };
        backingModel.getPersons().addListener(modelChangeListener);
        backingTagList().addListener(modelChangeListener);

        this.prefs = prefs;
    }

    public ModelManager(UserPrefs prefs) {
        this(new AddressBook(), prefs);
    }

    /**
     * Clears existing backing model and replaces with the provided new data.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        backingModel.resetData(newData);
    }

    public void initData(ReadOnlyAddressBook initialData) {
        resetData(initialData);
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
     * @return reference to the tags list inside backing model
     */
    private ObservableList<Tag> backingTagList() {
        return backingModel.getTags();
    }

    AddressBook backingModel() {
        return backingModel;
    }

    ViewableAddressBook visibleModel() {
        return visibleModel;
    }

//// MODEL CHANGE COMMANDS

    /**
     * Request to create a person. Simulates the change optimistically until remote confirmation, and provides a grace
     * period for cancellation, editing, or deleting.
     * @param userInputRetriever a callback to retrieve the user's input. Will be run on fx application thread
     */
    public synchronized void createPersonThroughUI(Callable<Optional<ReadOnlyPerson>> userInputRetriever) {
        final Supplier<Optional<ReadOnlyPerson>> fxThreadInputRetriever = () ->
                PlatformExecUtil.callAndWait(userInputRetriever, Optional.empty());
        execNewAddPersonCommand(fxThreadInputRetriever);
    }

    /**
     * Request to updaate a person. Simulates the change optimistically until remote confirmation, and provides a grace
     * period for cancellation, editing, or deleting. TODO listen on Person properties and not manually raise events
     * @param target The Person to be changed.
     * @param userInputRetriever callback to retrieve user's input. Will be run on fx application thread
     */
    public synchronized void editPersonThroughUI(ReadOnlyPerson target,
                                                 Callable<Optional<ReadOnlyPerson>> userInputRetriever) {
        final Supplier<Optional<ReadOnlyPerson>> fxThreadInputRetriever = () ->
                PlatformExecUtil.callAndWait(userInputRetriever, Optional.empty());

        if (personHasOngoingChange(target)) {
            getOngoingChangeForPerson(target.getId()).editInGracePeriod(fxThreadInputRetriever);
        } else {
            final ViewablePerson toEdit = visibleModel.findPerson(target).get();
            execNewEditPersonCommand(toEdit, fxThreadInputRetriever);
        }
    }

    /**
     * Request to delete a person. Simulates the change optimistically until remote confirmation, and provides a grace
     * period for cancellation, editing, or deleting.
     */
    public synchronized void deletePersonThroughUI(ReadOnlyPerson target) {
        if (personHasOngoingChange(target)) {
            getOngoingChangeForPerson(target.getId()).deleteInGracePeriod();
        } else {
            final ViewablePerson toDelete = visibleModel.findPerson(target).get();
            execNewDeletePersonCommand(toDelete);
        }
    }

    /**
     * Request to cancel any ongoing commands (add, edit, delete etc.) on the target person. Only works if the
     * ongoing command is in the pending state.
     */
    public synchronized void cancelPersonChangeCommand(ReadOnlyPerson target) {
        final ChangePersonInModelCommand ongoingCommand = getOngoingChangeForPerson(target.getId());
        if (ongoingCommand != null) {
            ongoingCommand.cancelInGracePeriod();
        }
    }

    void execNewAddPersonCommand(Supplier<Optional<ReadOnlyPerson>> inputRetriever) {
        final int GRACE_PERIOD_DURATION = 3;
        commandExecutor.execute(new AddPersonCommand(inputRetriever, GRACE_PERIOD_DURATION, this::raise, this));
    }

    void execNewEditPersonCommand(ViewablePerson target, Supplier<Optional<ReadOnlyPerson>> editInputRetriever) {
        final int GRACE_PERIOD_DURATION = 3;
        commandExecutor.execute(new EditPersonCommand(target, editInputRetriever, GRACE_PERIOD_DURATION,
                this::raise, this));
    }

    void execNewDeletePersonCommand(ViewablePerson target) {
        final int GRACE_PERIOD_DURATION = 3;
        commandExecutor.execute(new DeletePersonCommand(target, GRACE_PERIOD_DURATION, this::raise, this));
    }

    /**
     * @param changeInProgress the active change command on the person with id {@code targetPersonId}
     */
    synchronized void assignOngoingChangeToPerson(ReadOnlyPerson target, ChangePersonInModelCommand changeInProgress) {
        assignOngoingChangeToPerson(target.getId(), changeInProgress);
    }

    synchronized void assignOngoingChangeToPerson(int targetId, ChangePersonInModelCommand changeInProgress) {
        assert targetId == changeInProgress.getTargetPersonId() : "Must map to correct id";
        if (personChangesInProgress.containsKey(targetId)) {
            throw new IllegalStateException("Only 1 ongoing change allowed per person.");
        }
        personChangesInProgress.put(targetId, changeInProgress);
    }

    /**
     * Removed the target person's mapped changeInProgress, freeing it for other change commands.
     * @return the removed change command, or null if there was no mapping found
     */
    synchronized ChangePersonInModelCommand unassignOngoingChangeForPerson(ReadOnlyPerson person) {
        return personChangesInProgress.remove(person.getId());
    }

    synchronized ChangePersonInModelCommand unassignOngoingChangeForPerson(int targetId) {
        return personChangesInProgress.remove(targetId);
    }

    synchronized ChangePersonInModelCommand getOngoingChangeForPerson(ReadOnlyPerson person) {
        return getOngoingChangeForPerson(person.getId());
    }

    synchronized ChangePersonInModelCommand getOngoingChangeForPerson(int targetId) {
        return personChangesInProgress.get(targetId);
    }

    boolean personHasOngoingChange(ReadOnlyPerson key) {
        return personHasOngoingChange(key.getId());
    }

    boolean personHasOngoingChange(int personId) {
        return personChangesInProgress.containsKey(personId);
    }

    void raiseLocalModelChangedEvent() {
        raise(new LocalModelChangedEvent(this));
    }

//// CREATE

    /**
     * Manually add a ViewablePerson to the visible model
     */
    synchronized void addViewablePerson(ViewablePerson vp) {
        visibleModel.addPerson(vp);
    }

    /**
     * Manually add person to backing model without auto-creating a {@link ViewablePerson} for it in the visible model.
     */
    synchronized void addPersonToBackingModelSilently(Person p) {
        visibleModel.specifyViewableAlreadyCreated(p.getId());
        backingModel.addPerson(p);
    }

    // deprecated, to replace by remote assignment
    public int generatePersonId() {
        int id;
        do {
            id = Math.abs(UUID.randomUUID().hashCode());
        } while (id == 0 || backingModel.containsPerson(id));
        return id;
    }

    /**
     * Adds a tag to the model
     * @param tagToAdd
     * @throws DuplicateTagException when this operation would cause duplicates
     */
    public synchronized void addTagToBackingModel(Tag tagToAdd) throws DuplicateTagException {
        if (backingTagList().contains(tagToAdd)) {
            throw new DuplicateTagException(tagToAdd);
        }
        backingTagList().add(tagToAdd);
    }

//// UPDATE

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
        raise(new LocalModelChangedEvent(this));
    }

//// DELETE

    /**
     * Deletes the tag from the model.
     * @param tagToDelete
     * @return true if there was a successful removal
     */
    public synchronized boolean deleteTag(Tag tagToDelete) {
        return backingTagList().remove(tagToDelete);
    }

//// EVENT HANDLERS

    @Subscribe
    private <T> void handleUpdateCompletedEvent(SyncUpdateResourceCompletedEvent<T> uce) {
        // Sync is done outside FX Application thread
        // TODO: Decide how incoming updates should be handled
    }

//// DIFFERENTIAL UPDATE ENGINE todo shift this logic to sync component (with conditional requests to remote)

    /**
     * Diffs extData with the current model and updates the current model with minimal change.
     * @param extData data from an external canonical source
     */
    public synchronized void updateUsingExternalData(ReadOnlyAddressBook extData) {
        final AddressBook data = new AddressBook(extData);
        assert !data.containsDuplicates() : "Duplicates are not allowed in an AddressBook";
        boolean hasPersonsUpdates = diffUpdate(backingModel.getPersons(), data.getPersons());
        boolean hasTagsUpdates = diffUpdate(backingTagList(), data.getTags());
        if (hasPersonsUpdates || hasTagsUpdates) {
            raise(new LocalModelChangedEvent(this));
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

    public UserPrefs getPrefs() {
        return prefs;
    }

    public void setPrefsSaveLocation(String saveLocation) {
        prefs.setSaveLocation(saveLocation);
        raise(new SaveLocationChangedEvent(saveLocation));
        raise(new SavePrefsRequestEvent(prefs));
    }

    public void clearPrefsSaveLocation() {
        setPrefsSaveLocation(null);
    }

}
