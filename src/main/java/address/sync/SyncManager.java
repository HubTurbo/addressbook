package address.sync;


import address.events.*;
import address.exceptions.SyncErrorException;
import address.model.datatypes.AddressBook;
import address.model.datatypes.tag.Tag;
import address.model.datatypes.person.Person;
import address.sync.task.CloudUpdateTask;
import com.google.common.eventbus.Subscribe;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Syncs data between the cloud and the primary data file
 */
public class SyncManager {
    private static final Logger logger = LogManager.getLogger(SyncManager.class);

    private final ScheduledExecutorService scheduler;
    private final ExecutorService requestExecutor;
    private Optional<String> activeAddressBook;

    private CloudService cloudService;

    private LocalDateTime lastSuccessfulPersonsUpdate;

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

    public void startSyncingData(long interval, boolean simulateUnreliableNetwork) {
        if (interval <= 0) return;
        if (cloudService == null) {
            this.cloudService = new CloudService(simulateUnreliableNetwork);
        }
        updatePeriodically(interval);
    }

    /**
     * Runs periodically and posts results of updates as events
     *
     * @param interval number of units to wait
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
            } catch (SyncErrorException e) {
                logger.info("Error obtaining person updates.");
                EventManager.getInstance().post(new SyncFailedEvent(e.getMessage()));
            }

            EventManager.getInstance().post(new SyncCompletedEvent());
        };

        long initialDelay = 300; // temp fix for issue #66
        scheduler.scheduleWithFixedDelay(task, initialDelay, interval, TimeUnit.MILLISECONDS);
    }

    private AddressBook wrapWithAddressBook(List<Person> personList, List<Tag> tagList) {
        AddressBook wrapper = new AddressBook();
        wrapper.setPersons(personList);
        wrapper.setTags(tagList);
        return wrapper;
    }

    /**
     * Gets the list of updated persons since lastSuccessfulPersonsUpdate
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
