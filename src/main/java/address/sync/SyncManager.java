package address.sync;


import address.events.EventManager;
import address.events.LocalModelChangedEvent;
import address.events.NewMirrorDataEvent;
import address.events.SaveRequestEvent;
import address.exceptions.FileContainsDuplicatesException;
import address.model.AddressBook;
import address.prefs.PrefsManager;
import address.sync.task.CloudUpdateTask;
import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Syncs data between a mirror file and the primary data file
 */
public class SyncManager {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService requestExecutor = Executors.newCachedThreadPool();

    private CloudSimulator cloudSimulator = new CloudSimulator(false);

    public SyncManager() {
        EventManager.getInstance().registerHandler(this);
    }

    public void startSyncingData(long interval, boolean simulateUnreliableNetwork) {
        if (interval <= 0) return;
        this.cloudSimulator = new CloudSimulator(simulateUnreliableNetwork);
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
                Optional<AddressBook> mirrorData = getMirrorData();
                if (!mirrorData.isPresent()) {
                    System.out.println("Unable to retrieve data from mirror, cancelling sync...");
                    return;
                }
                EventManager.getInstance().post(new NewMirrorDataEvent(mirrorData.get()));
            } catch (FileContainsDuplicatesException e) {
                // do not sync changes from mirror if duplicates found in mirror
                System.out.println("Duplicate data found in mirror, cancelling sync...");
            }
        };

        long initialDelay = 300; // temp fix for issue #66
        scheduler.scheduleWithFixedDelay(task, initialDelay, interval, TimeUnit.MILLISECONDS);
    }

    private Optional<AddressBook> getMirrorData() throws FileContainsDuplicatesException {
        System.out.println("Updating data from cloud: " + System.nanoTime());
        final File mirrorFile = PrefsManager.getInstance().getMirrorFile();
        final Optional<AddressBook> data = cloudSimulator.getSimulatedCloudData(mirrorFile);
        if (data.isPresent()) {
            if (data.get().containsDuplicates()) throw new FileContainsDuplicatesException(mirrorFile);
        }
        return data;
    }

    @Subscribe
    public void handleLocalModelChangedEvent(LocalModelChangedEvent lmce) {
        requestExecutor.execute(new CloudUpdateTask(this.cloudSimulator, lmce.personData, lmce.groupData));
    }

    @Subscribe
    public void handleSaveRequestEvent(SaveRequestEvent sre) {
        requestExecutor.execute(new CloudUpdateTask(this.cloudSimulator, sre.personData, sre.groupData));
    }
}
