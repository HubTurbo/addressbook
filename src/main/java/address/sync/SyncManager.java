package address.sync;


import address.events.*;
import address.exceptions.FileContainsDuplicatesException;
import address.model.datatypes.AddressBook;
import address.model.datatypes.tag.Tag;
import address.model.datatypes.person.Person;
import address.prefs.PrefsManager;
import address.sync.task.CloudUpdateTask;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.*;
import address.util.LogManager;


/**
 * Syncs data between a mirror file and the primary data file
 */
public class SyncManager {
    private static final Logger logger = LogManager.getLogger(SyncManager.class.getName());

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService requestExecutor = Executors.newCachedThreadPool();

    private CloudService cloudService;

    public SyncManager() {
        EventManager.getInstance().registerHandler(this);
    }

    public void startSyncingData(long interval, boolean simulateUnreliableNetwork) {
        if (interval <= 0) return;
        this.cloudService = new CloudService(simulateUnreliableNetwork);
        updatePeriodically(interval);
    }

    /**
     * Runs periodically and adds any entries in the mirror file that is missing
     * in the primary data file. 
     * @param interval number of units to wait
     */
    public void updatePeriodically(long interval) {
        Runnable task = () -> {
            try {
                EventManager.getInstance().post(new SyncInProgressEvent());
                Optional<AddressBook> mirrorData = getMirrorData();
                if (!mirrorData.isPresent()) {
                    logger.info("Unable to retrieve data from mirror, cancelling sync...");
                    return;
                }
                EventManager.getInstance().post(new NewMirrorDataEvent(mirrorData.get()));
            } catch (FileContainsDuplicatesException e) {
                // do not sync changes from mirror if duplicates found in mirror
                logger.info("Duplicate data found in mirror, cancelling sync...");
            } finally {
                EventManager.getInstance().post(new SyncCompletedEvent());
            }
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

    private Optional<AddressBook> getMirrorData() throws FileContainsDuplicatesException {
        logger.info("Updating data from cloud.");

        final File mirrorFile = PrefsManager.getInstance().getMirrorLocation();

        try {
            // TODO: default should be changed to the desired addressbook's name
            ExtractedCloudResponse<List<Person>> personsResponse = cloudService.getPersons("default");
            ExtractedCloudResponse<List<Tag>> tagsResponse = cloudService.getTags("default");
            List<Person>  personList = personsResponse.getData().get();
            List<Tag> tagList = tagsResponse.getData().get();
            AddressBook data = wrapWithAddressBook(personList, tagList);
            if (data.containsDuplicates()) throw new FileContainsDuplicatesException(mirrorFile);

            return Optional.of(data);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        } catch (NoSuchElementException e) {
            logger.debug("Empty response from cloud!");
            return Optional.empty();
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
