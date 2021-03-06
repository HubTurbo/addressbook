package address.sync.task;

import address.events.*;
import address.events.sync.SyncCompletedEvent;
import address.events.sync.SyncFailedEvent;
import address.events.sync.SyncStartedEvent;
import address.exceptions.SyncErrorException;
import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;
import address.sync.RemoteManager;
import address.util.AppLogger;
import address.util.LoggerManager;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GetUpdatesFromRemoteTask implements Runnable {
    private static final AppLogger logger = LoggerManager.getLogger(GetUpdatesFromRemoteTask.class);
    private final Consumer<BaseEvent> eventRaiser;
    private final Supplier<Optional<String>> syncActiveAddressBookNameSupplier;
    private final RemoteManager remoteManager;

    public GetUpdatesFromRemoteTask(RemoteManager remoteManager, Consumer<BaseEvent> eventRaiser,
                                    Supplier<Optional<String>> syncActiveAddressBookNameSupplier) {
        this.eventRaiser = eventRaiser;
        this.syncActiveAddressBookNameSupplier = syncActiveAddressBookNameSupplier;
        this.remoteManager = remoteManager;
    }

    @Override
    public void run() {
        logger.info("Attempting to run periodic update.");
        eventRaiser.accept(new SyncStartedEvent());
        Optional<String> syncActiveAddressBookName = syncActiveAddressBookNameSupplier.get();
        if (!syncActiveAddressBookName.isPresent()) {
            eventRaiser.accept(new SyncFailedEvent("No active addressbook sync found."));
            return;
        }
        try {
            List<Person> updatedPersons = getUpdatedPersons(syncActiveAddressBookName.get());
            logger.debug("Updated persons: {}", updatedPersons);
            Optional<List<Tag>> latestTags = getLatestTags(syncActiveAddressBookName.get());
            logger.debug("Latest tags: {}", latestTags);

            eventRaiser.accept(new SyncCompletedEvent(updatedPersons, latestTags));
        } catch (SyncErrorException e) {
            logger.warn("Error obtaining updates: {}", e);
            eventRaiser.accept(new SyncFailedEvent(e.getMessage()));
        } catch (Exception e) {
            logger.warn("Exception occurred in update task: {}", e);
            eventRaiser.accept(new SyncFailedEvent(e.getMessage()));
        }
    }

    /**
     * Gets the list of persons that have been updated on the remote since the last request
     *
     * @param addressBookName
     * @return
     * @throws SyncErrorException if bad response code, missing data or network error
     */
    private List<Person> getUpdatedPersons(String addressBookName) throws SyncErrorException {
        try {
            Optional<List<Person>> updatedPersons = remoteManager.getUpdatedPersons(addressBookName);
            if (!updatedPersons.isPresent()) throw new SyncErrorException("getUpdatedPersons failed.");
            return updatedPersons.get();
        } catch (IOException e) {
            throw new SyncErrorException("Error getting updated persons.");
        }
    }

    /**
     * Gets the full list of tags if it has been updated since the last request
     *
     * @param addressBookName
     * @return
     * @throws SyncErrorException if bad response code, missing data or network error
     */
    private Optional<List<Tag>> getLatestTags(String addressBookName) throws SyncErrorException {
        try {
            return remoteManager.getLatestTagList(addressBookName);
        } catch (IOException e) {
            throw new SyncErrorException("Error getting latest tags.");
        }
    }
}
