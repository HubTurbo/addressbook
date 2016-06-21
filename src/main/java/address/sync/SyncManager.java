package address.sync;


import address.events.*;
import address.exceptions.SyncErrorException;
import address.model.datatypes.tag.Tag;
import address.model.datatypes.person.Person;
import address.sync.task.CloudUpdateTask;
import address.util.AppLogger;
import address.util.LoggerManager;
import com.google.common.eventbus.Subscribe;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Syncs data between the cloud and the primary data file
 */
public class SyncManager {
    private static final AppLogger logger = LoggerManager.getLogger(SyncManager.class);

    private final ScheduledExecutorService scheduler;
    private final ExecutorService requestExecutor;
    private Optional<String> activeAddressBook;

    private RemoteManager remoteManager;

    public SyncManager() {
        activeAddressBook = Optional.empty();
        scheduler = Executors.newScheduledThreadPool(1);
        requestExecutor = Executors.newCachedThreadPool();
        EventManager.getInstance().registerHandler(this);
    }
    public SyncManager(RemoteManager remoteManager, ExecutorService executorService,
                       ScheduledExecutorService scheduledExecutorService) {
        this.remoteManager = remoteManager;
        this.scheduler = scheduledExecutorService;
        this.requestExecutor = executorService;
        activeAddressBook = Optional.empty();
        EventManager.getInstance().registerHandler(this);
    }

    // TODO: setActiveAddressBook should be called by the model instead
    @Subscribe
    public void handleLoadDataRequestEvent(LoadDataRequestEvent e) {
        setActiveAddressBook(e.file.getName());
    }

    public void setActiveAddressBook(String activeAddressBookName) {
        logger.info("Active addressbook set to {}", activeAddressBookName);
        activeAddressBook = Optional.of(activeAddressBookName);
    }

    /**
     * Initializes the remote service, if it hasn't been
     *
     * Starts getting periodic updates from the cloud
     *
     * @param interval should be a positive integer
     */
    public void startSyncingData(long interval) {
        if (interval <= 0) {
            logger.warn("Update interval specified is not positive: {}", interval);
            return;
        }
        if (remoteManager == null) {
            this.remoteManager = new RemoteManager();
        }
        updatePeriodically(interval);
    }

    /**
     * Runs an update task periodically every interval milliseconds
     *
     * Raises a SyncStartedEvent at the beginning, and SyncFailedEvent or SyncCompletedEvent at the end of the task
     * Raises UpdateCompletedEvent after each resource update is finished successfully
     *
     * @param interval number of milliseconds to wait
     */
    public void updatePeriodically(long interval) {
        Runnable task = () -> {
            logger.info("Attempting to run periodic update.");
            EventManager.getInstance().post(new SyncStartedEvent());

            if (!activeAddressBook.isPresent()) {
                EventManager.getInstance().post(new SyncFailedEvent("No active addressbook sync found."));
                return;
            }
            try {
                List<Person> updatedPersons = getUpdatedPersons(activeAddressBook.get());
                logger.logList("Found updated persons: {}", updatedPersons);
                EventManager.getInstance().post(
                        new UpdateCompletedEvent<>(updatedPersons, "Person updates completed."));

                List<Tag> updatedTagList = getUpdatedTags(activeAddressBook.get());
                EventManager.getInstance().post(new UpdateCompletedEvent<>(updatedTagList, "Tag updates completed."));

                EventManager.getInstance().post(new SyncCompletedEvent());
            } catch (SyncErrorException e) {
                logger.warn("Error obtaining updates.");
                EventManager.getInstance().post(new SyncFailedEvent(e.getMessage()));
            } catch (Exception e) {e.printStackTrace();

                logger.warn("{}", e);
            }
        };

        long initialDelay = 300; // temp fix for issue #66
        scheduler.scheduleWithFixedDelay(task, initialDelay, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the list of persons that have been updated since the last request
     *
     * @param addressBookName
     * @return
     * @throws SyncErrorException if bad response code, missing data or network error
     */
    private List<Person> getUpdatedPersons(String addressBookName) throws SyncErrorException {
        try {
            Optional<List<Person>> updatedPersons;
            updatedPersons = remoteManager.getUpdatedPersons(addressBookName);

            if (!updatedPersons.isPresent()) throw new SyncErrorException("getUpdatedPersons failed.");

            logger.debug("Updated persons retrieved.");
            return updatedPersons.get();
        } catch (IOException e) {
            throw new SyncErrorException("Error getting updated persons.");
        }
    }

    private List<Tag> getUpdatedTags(String addressBookName) throws SyncErrorException {
        try {
            Optional<List<Tag>> updatedTags = remoteManager.getUpdatedTagList(addressBookName);

            if (!updatedTags.isPresent()) {
                logger.info("No updates to tags.");
                return null;
            } else {
                logger.info("Updated tags: {}", updatedTags);
                return updatedTags.get();
            }
        } catch (IOException e) {
            throw new SyncErrorException("Error getting updated persons.");
        }
    }

    @Subscribe
    public void handleLocalModelChangedEvent(LocalModelChangedEvent lmce) {
        requestExecutor.execute(new CloudUpdateTask(this.remoteManager, lmce.data));
    }

    // To be removed after working out specification on saving and syncing behaviour
    @Subscribe
    public void handleSaveRequestEvent(SaveRequestEvent sre) {
        requestExecutor.execute(new CloudUpdateTask(this.remoteManager, sre.data));
    }
}
