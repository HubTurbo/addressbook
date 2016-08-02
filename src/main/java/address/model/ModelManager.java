package address.model;

import address.events.model.LocalModelChangedEvent;
import address.exceptions.DuplicateTagException;
import address.main.ComponentManager;
import address.model.datatypes.AddressBook;
import address.model.datatypes.ReadOnlyAddressBook;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.tag.Tag;
import address.util.AppLogger;
import address.util.Config;
import address.util.LoggerManager;
import address.util.collections.UnmodifiableObservableList;
import commons.PlatformExecUtil;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Represents the in-memory model of the address book data.
 * All changes to any model should be synchronized.
 */
public class ModelManager extends ComponentManager implements ReadOnlyAddressBook {
    private static final AppLogger logger = LoggerManager.getLogger(ModelManager.class);

    private final AddressBook backingModel;

    private final Executor commandExecutor;
    private final AtomicInteger commandCounter;

    private String saveFilePath;
    private String addressBookNameToUse;

    public static final int GRACE_PERIOD_DURATION = 3;

    {
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

//// UI COMMANDS

    /**
     * Request to create a person. Simulates the change optimistically until remote confirmation, and provides a grace
     * period for cancellation, editing, or deleting.
     */
    public synchronized void createPersonThroughUI(Optional<ReadOnlyPerson> person) {
        Person toAdd;
        do { // make sure no id clashes.
            toAdd = new Person(Math.abs(UUID.randomUUID().hashCode()));
        } while (backingModel.getPersonList().contains(toAdd));
        toAdd.update(person.get());
        backingModel.addPerson(toAdd);
        updateBackingStorage();
    }

    /**
     * Request to update a person. Simulates the change optimistically until remote confirmation, and provides a grace
     * period for cancellation, editing, or deleting. TODO listen on Person properties and not manually raise events
     * @param target The Person to be changed.
     */
    public synchronized void editPersonThroughUI(ReadOnlyPerson target,
                                                 Optional<ReadOnlyPerson> editedTarget) {
        backingModel.findPerson(target).get().update(editedTarget.get());
        updateBackingStorage();
    }

    private void updateBackingStorage() {
        raise(new LocalModelChangedEvent(backingModel));
    }

    /**
     * Request to set the tags for a group of Persons. Simulates change optimistically until remote confirmation,
     * and provides a grace period for cancellation, editing, or deleting.
     * @param targets Persons to be retagged
     */
    public void retagPersonsThroughUI(Collection<? extends ReadOnlyPerson> targets,
                                      Optional<? extends Collection<Tag>> newTags) {

        targets.stream().forEach(p -> backingModel.findPerson(p).get().setTags(newTags.get()));
        updateBackingStorage();
    }

    /**
     * Request to delete a person. Simulates the change optimistically until remote confirmation, and provides a grace
     * period for cancellation, editing, or deleting.
     */
    public synchronized void deletePersonThroughUI(ReadOnlyPerson target) {
        backingModel.removePerson(target);
        updateBackingStorage();
    }

//// CREATE

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
        String originalName = original.getName();
        original.update(updated);
    }

//// DELETE

    /**
     * Deletes the tag from the model.
     * @param tagToDelete
     * @return true if there was a successful removal
     */
    public synchronized boolean deleteTag(Tag tagToDelete) {
        boolean result = backingTagList().remove(tagToDelete);
        return result;
    }

    /**
     * @return true if there were changes (syncdata not empty)
     */
    private boolean syncPersons(Collection<Person> syncData) {
        Set<Integer> deletedPersonIds = new HashSet<>();
        Map<Integer, ReadOnlyPerson> newOrUpdatedPersons = new HashMap<>();
        syncData.forEach(p -> {
            if (p.isDeleted()) {
                deletedPersonIds.add(p.getId());
            } else {
                newOrUpdatedPersons.put(p.getId(), p);
            }
        });
        PlatformExecUtil.runAndWait(() -> {
            // removal
            backingModel.getPersons().removeAll(backingModel.getPersons().stream()
                    .filter(p -> deletedPersonIds.contains(p.getId())).collect(Collectors.toList()));
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
        return !syncData.isEmpty();
    }

    /**
     * @return true if there were changes
     */
    private boolean syncTags(Collection<Tag> syncData) {
        Set<Tag> latestTags = new HashSet<>(syncData);
        return backingModel.getTags().retainAll(latestTags) // delete
                // non short circuiting OR
                | PlatformExecUtil.callAndWait(() -> {
                    latestTags.removeAll(backingModel.getTags()); // latest tags no longer contains tags already in model
                    backingModel.getTags().addAll(latestTags); // add
                    return !latestTags.isEmpty();
                }, true);
    }
}
