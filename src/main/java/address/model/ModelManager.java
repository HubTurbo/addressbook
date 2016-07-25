package address.model;

import address.events.model.LocalModelChangedEvent;
import address.events.sync.CreateTagOnRemoteRequestEvent;
import address.events.sync.DeleteTagOnRemoteRequestEvent;
import address.events.sync.EditTagOnRemoteRequestEvent;
import address.events.sync.SyncCompletedEvent;
import address.exceptions.DuplicateTagException;
import address.main.ComponentManager;
import address.model.datatypes.*;
import address.model.datatypes.person.*;
import address.model.datatypes.tag.Tag;
import address.util.*;
import address.util.collections.UnmodifiableObservableList;
import com.google.common.eventbus.Subscribe;

import address.util.AppLogger;
import address.util.LoggerManager;
import commons.PlatformExecUtil;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Represents the in-memory model of the address book data.
 * All changes to any model should be synchronized.
 */
public class ModelManager extends ComponentManager implements ReadOnlyAddressBook, ReadOnlyViewableAddressBook {
    private static final AppLogger logger = LoggerManager.getLogger(ModelManager.class);

    private final AddressBook backingModel;
    private final ViewableAddressBook visibleModel;

    private final Map<Integer, ChangePersonInModelCommand> personChangesInProgress;
    private final Executor commandExecutor;
    private final AtomicInteger commandCounter;

    private String saveFilePath;
    private String addressBookNameToUse;

    {
        personChangesInProgress = new HashMap<>();
        commandExecutor = Executors.newCachedThreadPool();
        commandCounter = new AtomicInteger(0);
    }

    /**
     * Initializes a ModelManager with the given AddressBook
     * AddressBook and its variables should not be null
     */
    public ModelManager(AddressBook src, Config config) {
        super();
        if (src == null) {
            logger.fatal("Attempted to initialize with a null AddressBook");
            assert false;
        }
        logger.debug("Initializing with address book: {}", src);

        backingModel = new AddressBook(src);
        visibleModel = backingModel.createVisibleAddressBook();

        this.saveFilePath = config.getLocalDataFilePath();
        this.addressBookNameToUse = config.getAddressBookName();
    }

    public ModelManager(Config config) {
        this(new AddressBook(), config);
    }

    public ReadOnlyAddressBook getDefaultAddressBook() {
        return new AddressBook();
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

    protected AddressBook backingModel() {
        return backingModel;
    }

    protected ViewableAddressBook visibleModel() {
        return visibleModel;
    }

//// UI COMMANDS

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
     * Request to update a person. Simulates the change optimistically until remote confirmation, and provides a grace
     * period for cancellation, editing, or deleting. TODO listen on Person properties and not manually raise events
     * @param target The Person to be changed.
     * @param userInputRetriever callback to retrieve user's input. Will be run on fx application thread
     */
    public synchronized void editPersonThroughUI(ReadOnlyPerson target,
                                                 Callable<Optional<ReadOnlyPerson>> userInputRetriever) {
        final Supplier<Optional<ReadOnlyPerson>> fxThreadInputRetriever = () ->
                PlatformExecUtil.callAndWait(userInputRetriever, Optional.empty());

        if (personHasOngoingChange(target)) {
            getOngoingChangeForPerson(target.getId()).overrideWithEditPerson(fxThreadInputRetriever);
        } else {
            final ViewablePerson toEdit = visibleModel.findPerson(target).get();
            execNewEditPersonCommand(toEdit, fxThreadInputRetriever);
        }
    }

    /**
     * Request to set the tags for a group of Persons. Simulates change optimistically until remote confirmation,
     * and provides a grace period for cancellation, editing, or deleting.
     * @param targets Persons to be retagged
     * @param newTagsRetriever callback to retrieve the tags to set for every person in {@code targets}.
     *                         Will be run on fx application thread
     */
    public void retagPersonsThroughUI(Collection<? extends ReadOnlyPerson> targets,
                                      Callable<Optional<? extends Collection<Tag>>> newTagsRetriever) {

        final CompletableFuture<Optional<? extends Collection<Tag>>> chosenTags = new CompletableFuture<>();
        final AtomicBoolean alreadyRetrieved = new AtomicBoolean(false);

        final Function<ReadOnlyPerson, Supplier<Optional<ReadOnlyPerson>>> editInputRetrieverFactory = p -> () -> {
            // run ui input retriever on this thread if no other thread did it yet
            boolean shouldRunTagsRetriever = !alreadyRetrieved.getAndSet(true);
            if (shouldRunTagsRetriever) {
                chosenTags.complete(PlatformExecUtil.callAndWait(newTagsRetriever, Optional.empty()));
            }

            try {
                Optional<? extends Collection<Tag>> chosenTagsInput = chosenTags.get();
                if (!chosenTagsInput.isPresent()) {
                    return Optional.empty();
                }
                Person afterRetag = new Person(p);
                afterRetag.setTags(chosenTagsInput.get());
                return Optional.of(afterRetag);

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        };
        // handle edit commands for each target
        targets.forEach(target -> {
            if (personHasOngoingChange(target)) {
                getOngoingChangeForPerson(target.getId()).overrideWithEditPerson(editInputRetrieverFactory.apply(target));
            } else {
                final ViewablePerson toEdit = visibleModel.findPerson(target).get();
                execNewEditPersonCommand(toEdit, editInputRetrieverFactory.apply(target));
            }
        });
    }

    /**
     * Request to delete a person. Simulates the change optimistically until remote confirmation, and provides a grace
     * period for cancellation, editing, or deleting.
     */
    public synchronized void deletePersonThroughUI(ReadOnlyPerson target) {
        if (personHasOngoingChange(target)) {
            getOngoingChangeForPerson(target.getId()).overrideWithDeletePerson();
        } else {
            final ViewablePerson toDelete = visibleModel.findPerson(target).get();
            execNewDeletePersonCommand(toDelete);
        }
    }

    /**
     * Request to cancel any ongoing commands (add, edit, delete etc.) on the target person. Only works if the
     * ongoing command is in the pending state.
     */
    public synchronized void cancelPersonCommand(ReadOnlyPerson target) {
        final ChangePersonInModelCommand ongoingCommand = getOngoingChangeForPerson(target.getId());
        if (ongoingCommand != null) {
            ongoingCommand.cancelCommand();
        }
    }

    /**
     * Request to retry any failed commands
     */
    public synchronized void retryFailedPersonCommand(ReadOnlyPerson target) {
        final ChangePersonInModelCommand ongoingCommand = getOngoingChangeForPerson(target.getId());
        if (ongoingCommand != null) {
            ongoingCommand.retry();
        }
    }

//// Command utilities

    protected void execNewAddPersonCommand(Supplier<Optional<ReadOnlyPerson>> inputRetriever) {
        final int GRACE_PERIOD_DURATION = 3;
        commandExecutor.execute(new AddPersonCommand(assignCommandId(), inputRetriever, GRACE_PERIOD_DURATION, this::raise, this, addressBookNameToUse));
    }

    protected void execNewEditPersonCommand(ViewablePerson target, Supplier<Optional<ReadOnlyPerson>> editInputRetriever) {
        final int GRACE_PERIOD_DURATION = 3;
        commandExecutor.execute(new EditPersonCommand(assignCommandId(), target, editInputRetriever,
                GRACE_PERIOD_DURATION, this::raise, this, addressBookNameToUse));
    }

    protected void execNewDeletePersonCommand(ViewablePerson target) {
        final int GRACE_PERIOD_DURATION = 3;
        commandExecutor.execute(new DeletePersonCommand(assignCommandId(), target, GRACE_PERIOD_DURATION, this::raise, this, addressBookNameToUse));
    }

    /**
     * @param changeInProgress the active change command on the person with id {@code targetPersonId}
     */
    protected synchronized void assignOngoingChangeToPerson(ReadOnlyPerson target, ChangePersonInModelCommand changeInProgress) {
        assignOngoingChangeToPerson(target.getId(), changeInProgress);
    }

    protected synchronized void assignOngoingChangeToPerson(int targetId, ChangePersonInModelCommand changeInProgress) {
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
    protected synchronized ChangePersonInModelCommand unassignOngoingChangeForPerson(ReadOnlyPerson person) {
        return personChangesInProgress.remove(person.getId());
    }

    protected synchronized ChangePersonInModelCommand unassignOngoingChangeForPerson(int targetId) {
        return personChangesInProgress.remove(targetId);
    }

    protected synchronized ChangePersonInModelCommand getOngoingChangeForPerson(ReadOnlyPerson person) {
        return getOngoingChangeForPerson(person.getId());
    }

    protected synchronized ChangePersonInModelCommand getOngoingChangeForPerson(int targetId) {
        return personChangesInProgress.get(targetId);
    }

    boolean personHasOngoingChange(ReadOnlyPerson key) {
        return personHasOngoingChange(key.getId());
    }

    boolean personHasOngoingChange(int personId) {
        return personChangesInProgress.containsKey(personId);
    }

    int assignCommandId() {
        return commandCounter.incrementAndGet();
    }

//// CREATE

    /**
     * Manually add a ViewablePerson without a backing person to the visible model
     * @return the added ViewablePerson
     */
    protected synchronized ViewablePerson addViewablePersonWithoutBacking(ReadOnlyPerson data) {
        final ViewablePerson toAdd = ViewablePerson.withoutBacking(data);
        visibleModel.addPerson(toAdd);
        return toAdd;
    }

    /**
     * Manually add person to backing model without auto-creating a {@link ViewablePerson} for it in the visible model.
     */
    protected synchronized void addPersonToBackingModelSilently(Person p) {
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
        raise(new CreateTagOnRemoteRequestEvent(new CompletableFuture<>(), addressBookNameToUse, tagToAdd));
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
        String originalName = original.getName();
        original.update(updated);
        raise(new EditTagOnRemoteRequestEvent(new CompletableFuture<>(),
                addressBookNameToUse, originalName, updated));
    }

//// DELETE

    /**
     * Deletes the tag from the model.
     * @param tagToDelete
     * @return true if there was a successful removal
     */
    public synchronized boolean deleteTag(Tag tagToDelete) {
        boolean result = backingTagList().remove(tagToDelete);
        raise(new DeleteTagOnRemoteRequestEvent(new CompletableFuture<>(),
                addressBookNameToUse, tagToDelete.getName()));
        return result;
    }

//// EVENT HANDLERS

    @Subscribe
    private void handleSyncCompletedEvent(SyncCompletedEvent uce) {
        // Sync is done outside FX Application thread
        if (uce.getLatestTags().isPresent()) {
            syncTags(uce.getLatestTags().get());
        }
        syncPersons(uce.getUpdatedPersons());
        raise(new LocalModelChangedEvent(this));
    }

    private void syncPersons(Collection<Person> syncData) {
        Set<Integer> deletedPersonIds = new HashSet<>();
        Map<Integer, ReadOnlyPerson> newOrUpdatedPersons = new HashMap<>();
        syncData.forEach(p -> {
            if (p.isDeleted()) {
                deletedPersonIds.add(p.getId());
            } else {
                newOrUpdatedPersons.put(p.getId(), p);
            }
        });
        PlatformExecUtil.runLater(() -> {
            // removal
            backingModel.getPersons().removeAll(backingModel.getPersons().stream()
                    .filter(p -> deletedPersonIds.contains(p.getId())).collect(Collectors.toList())); // removeIf() not optimised
            // edits
            backingModel.getPersons().forEach(p -> {
                if (newOrUpdatedPersons.containsKey(p.getId())) {
                    p.update(newOrUpdatedPersons.remove(p.getId()));
                }
            });
            // new
            backingModel.getPersons().addAll(newOrUpdatedPersons.values().stream()
                    .map(Person::new).collect(Collectors.toList()));
        });
    }

    private void syncTags(Collection<Tag> syncData) {
        Set<Tag> latestTags = new HashSet<>(syncData);
        backingModel.getTags().retainAll(latestTags); // delete
        PlatformExecUtil.runLater(() -> {
            latestTags.removeAll(backingModel.getTags()); // latest tags no longer contains tags already in model
            backingModel.getTags().addAll(latestTags); // add
        });
    }
}
