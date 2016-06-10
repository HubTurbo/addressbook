package address.sync;


import address.events.*;
import address.exceptions.SyncErrorException;
import address.model.datatypes.tag.Tag;
import address.model.datatypes.person.Person;
import address.sync.task.CloudUpdateTask;
import address.util.LoggerManager;
import com.google.common.eventbus.Subscribe;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Syncs data between the cloud and the primary data file
 */
public class SyncManager {
    private static final Logger logger = LoggerManager.getLogger(SyncManager.class.getName());

    private final ScheduledExecutorService scheduler;
    private final ExecutorService requestExecutor;
    private Optional<String> activeAddressBook;

    private CloudService cloudService;

    private LocalDateTime lastSuccessfulPersonsUpdate;
    private String lastTagsETag;
    private LocalDateTime lastSuccessfulTagsUpdate;

    public SyncManager() {
        activeAddressBook = Optional.empty();
        scheduler = Executors.newScheduledThreadPool(1);
        requestExecutor = Executors.newCachedThreadPool();
        EventManager.getInstance().registerHandler(this);
    }

    public SyncManager(CloudService cloudService, ExecutorService executorService, ScheduledExecutorService scheduledExecutorService) {
        this.cloudService = cloudService;
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
     * Initializes the cloud service, if it hasn't been
     *
     * Starts getting periodic updates from the cloud
     *
     * @param interval
     * @param simulateUnreliableNetwork
     */
    public void startSyncingData(long interval, boolean simulateUnreliableNetwork) {
        if (interval <= 0) return;
        if (cloudService == null) {
            this.cloudService = new CloudService(simulateUnreliableNetwork);
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
            logger.info("Attempting to update at {}", System.currentTimeMillis());
            EventManager.getInstance().post(new SyncStartedEvent());

            if (!activeAddressBook.isPresent()) {
                EventManager.getInstance().post(new SyncFailedEvent("No active addressbook sync found."));
                return;
            }
            try {
                List<Person> updatedPersons = getUpdatedPersons(activeAddressBook.get());
                logger.info("{} updated persons found.", updatedPersons.size());
                EventManager.getInstance().post(new UpdateCompletedEvent<>(updatedPersons, "Person updates completed."));

                Optional<List<Tag>> updatedTagList = getUpdatedTags(activeAddressBook.get());
                if (updatedTagList.isPresent()) {
                    logger.info("Acquired new list of {} tags.", updatedPersons.size());
                } else {
                    logger.info("No updates to tags.");
                }
                EventManager.getInstance().post(new UpdateCompletedEvent<>(updatedTagList, "Tag updates completed."));
            } catch (SyncErrorException e) {
                logger.info("Error obtaining updates.");
                EventManager.getInstance().post(new SyncFailedEvent(e.getMessage()));
                return;
            }

            EventManager.getInstance().post(new SyncCompletedEvent());
        };

        long initialDelay = 300; // temp fix for issue #66
        scheduler.scheduleWithFixedDelay(task, initialDelay, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the list of persons that have been updated since lastSuccessfulPersonsUpdate
     *
     * If lastSuccessfulPersonsUpdate is null, the full list of persons will be obtained instead
     *
     * @param addressBookName
     * @return
     * @throws SyncErrorException if bad response code, missing data or network error
     */
    private List<Person> getUpdatedPersons(String addressBookName) throws SyncErrorException {
        logger.info("Retrieving person data from cloud.");

        try {
            ExtractedCloudResponse<List<Person>> personsResponse;
            if (lastSuccessfulPersonsUpdate == null) {
                logger.debug("No previous persons update found.");
                personsResponse = cloudService.getPersons(addressBookName);
            } else {
                logger.debug("Last persons update at: {}", lastSuccessfulPersonsUpdate);
                 personsResponse = cloudService.getUpdatedPersonsSince(addressBookName, lastSuccessfulPersonsUpdate);
            }
            if (personsResponse.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new SyncErrorException(personsResponse.getResponseCode() + " response from cloud instead of expected " + HttpURLConnection.HTTP_OK + " during persons update.");
            }
            if (!personsResponse.getData().isPresent()) {
                throw new SyncErrorException("Unexpected missing data from response.");
            }
            lastSuccessfulPersonsUpdate = LocalDateTime.now();
            return personsResponse.getData().get();
        } catch (IOException e) {
            throw new SyncErrorException("Error getting updated persons.");
        }
    }

    private Optional<List<Tag>> getUpdatedTags(String addressBookName) throws SyncErrorException {
        logger.info("Retrieving tag data from cloud.");

        try {
            if (lastTagsETag == null) {
                logger.debug("No previous tag updates found.");
            } else {
                logger.debug("Found last tags update at: {}", lastSuccessfulTagsUpdate);
            }

            ExtractedCloudResponse<List<Tag>> tagsResponse = cloudService.getTags(addressBookName, lastTagsETag);
            switch (tagsResponse.getResponseCode()) {
                case HttpURLConnection.HTTP_OK:
                    if (!tagsResponse.getData().isPresent()) {
                        throw new SyncErrorException("Unexpected missing data from response.");
                    }
                    lastTagsETag = tagsResponse.getETag();
                    // fallthrough
                case HttpURLConnection.HTTP_NOT_MODIFIED:
                    lastSuccessfulTagsUpdate = LocalDateTime.now();
                    return tagsResponse.getData();
                default:
                    throw new SyncErrorException(tagsResponse.getResponseCode() + " response from cloud instead of expected " + HttpURLConnection.HTTP_OK + " or " + HttpURLConnection.HTTP_NOT_MODIFIED + " during tags update.");

            }
        } catch (IOException e) {
            throw new SyncErrorException("Error getting updated tags.");
        }
    }

    @Subscribe
    public void handleLocalModelChangedEvent(LocalModelChangedEvent lmce) {
        requestExecutor.execute(new CloudUpdateTask(this.cloudService, lmce.personData, lmce.tagData));
    }

    // To be removed after working out specification on saving and syncing behaviour
    @Subscribe
    public void handleSaveRequestEvent(SaveRequestEvent sre) {
        requestExecutor.execute(new CloudUpdateTask(this.cloudService, sre.personData, sre.tagData));
    }
}
