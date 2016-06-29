package address.sync;


import address.events.*;
import address.exceptions.SyncErrorException;
import address.main.ComponentManager;
import address.model.datatypes.tag.Tag;
import address.model.datatypes.person.Person;
import address.sync.task.*;
import address.util.AppLogger;
import address.util.Config;
import address.util.LoggerManager;
import com.google.common.eventbus.Subscribe;

import java.util.Optional;
import java.util.concurrent.*;

/**
 * Syncs data between the local model and remote
 *
 * Once started, attempts to synchronize with the remote periodically
 * Synchronization will be based on the currently-active address book which can be set via setActiveAddressBook
 */
public class SyncManager extends ComponentManager {
    private static final AppLogger logger = LoggerManager.getLogger(SyncManager.class);

    private final ScheduledExecutorService scheduler;
    private final ExecutorService requestExecutor;
    private final RemoteManager remoteManager;
    private final Config config;

    private Optional<String> activeAddressBook;

    /**
     * Constructor for SyncManager
     *
     * @param config should have updateInterval (milliseconds) and simulateUnreliableNetwork set
     */
    public SyncManager(Config config) {
        this(config, new RemoteManager(config), Executors.newCachedThreadPool(), Executors.newScheduledThreadPool(1));
    }

    /**
     * Constructor for SyncManager
     *
     * @param config should have updateInterval (milliseconds) and simulateUnreliableNetwork set
     * @param remoteManager
     * @param executorService
     * @param scheduledExecutorService
     */
    public SyncManager(Config config, RemoteManager remoteManager, ExecutorService executorService,
                       ScheduledExecutorService scheduledExecutorService) {
        super();
        activeAddressBook = Optional.empty();
        this.config = config;
        this.remoteManager = remoteManager;
        this.requestExecutor = executorService;
        this.scheduler = scheduledExecutorService;

    }

    // TODO: setActiveAddressBook should be called by the model instead
    // For now, assume that the address book's save file name is the name of the addressbook
    @Subscribe
    public void handleSaveLocationChangedEvent(SaveLocationChangedEvent slce) {
        if (slce.saveFile == null) {
            setActiveAddressBook(null);
            return;
        }
        setActiveAddressBook(slce.saveFile.getName());
    }

    public Optional<String> getActiveAddressBook() {
        return activeAddressBook;
    }

    /**
     * Sets the currently active addressbook for periodic updates
     *
     * @param activeAddressBookName if null, subsequent updates will fail until re-set to a valid address book
     */
    public void setActiveAddressBook(String activeAddressBookName) {
        logger.info("Active addressbook set to {}", activeAddressBookName);
        activeAddressBook = Optional.ofNullable(activeAddressBookName);
    }

    /**
     * Starts synchronizing with the cloud, after every updateInterval milliseconds
     * specified in the config.
     * Synchronization will fail if active address book is not set or is invalid.
     *
     * Raises a SyncStartedEvent at the beginning, and SyncFailedEvent or SyncCompletedEvent at the end of the task
     * Raises a SyncUpdateResourceCompletedEvent after each resource update is finished successfully
     */
    public void start() {
        logger.info("Starting sync manager.");
        long initialDelay = 300; // temp fix for issue #66
        Runnable syncTask = new GetUpdatesFromRemoteTask(remoteManager, this::raise, this::getActiveAddressBook);
        logger.debug("Scheduling synchronization task with interval of {} milliseconds", config.updateInterval);
        scheduler.scheduleWithFixedDelay(syncTask, initialDelay, config.updateInterval, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        logger.info("Stopping sync manager.");
        scheduler.shutdown();
        requestExecutor.shutdown();
    }

    public Future<Person> createPerson(String addressBookName, Person createdPerson) throws SyncErrorException {
        return executeTask(new CreatePersonOnRemoteTask(remoteManager, addressBookName, createdPerson));
    }

    public Future<Tag> createTag(String addressBookName, Tag createdTag) throws SyncErrorException {
        return executeTask(new CreateTagOnRemoteTask(remoteManager, addressBookName, createdTag));
    }

    public Future<Tag> editTag(String addressBookName, String tagName, Tag editedTag) throws SyncErrorException {
        return executeTask(new EditTagOnRemoteTask(remoteManager, addressBookName, tagName, editedTag));
    }

    public Future<Person> updatePerson(String addressBookName, int personId, Person updatedPerson)
            throws SyncErrorException {
        return executeTask(new UpdatePersonOnRemoteTask(remoteManager, addressBookName, personId,
                                                        updatedPerson));
    }

    public Future<Boolean> deletePerson(String addressBookName, int personId) throws SyncErrorException {
        return executeTask(new DeletePersonOnRemoteTask(remoteManager, addressBookName, personId));
    }

    public Future<Boolean> deleteTag(String addressBookName, String tagName) throws SyncErrorException {
        return executeTask(new DeleteTagOnRemoteTask(remoteManager, addressBookName, tagName));
    }

    public Future<Boolean> createAddressBook(String addressBookName) throws SyncErrorException {
        return executeTask(new CreateAddressBookOnRemote(remoteManager, addressBookName));
    }

    private <T> Future<T> executeTask(RemoteTaskWithResult<T> task) {
        return requestExecutor.submit(task);
    }
}
