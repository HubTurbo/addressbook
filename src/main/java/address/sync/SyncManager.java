package address.sync;


import address.events.EventManager;
import address.events.LocalModelChangedEvent;
import address.events.NewMirrorDataEvent;
import address.exceptions.FileContainsDuplicatesException;
import address.model.AddressBookWrapper;
import address.preferences.PreferencesManager;
import address.sync.task.CloudUpdateTask;
import com.google.common.eventbus.Subscribe;

import java.io.File;
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

    public void startSyncingData(long interval, boolean isSimulateRandomChanges) {
        if (interval <= 0) return;
        this.cloudSimulator = new CloudSimulator(isSimulateRandomChanges);
        updatePeriodically(interval);
    }

    /**
     * Runs periodically and adds any entries in the mirror file that is missing
     * in the primary data file. The mirror file should be at the same location
     * as primary file and the name should be '{primary file name}-mirror.xml'.
     * @param interval The period between updates
     */
    public void updatePeriodically(long interval) {
        Runnable task = () -> {
            try {
                AddressBookWrapper mirrorData = getMirrorData();
                EventManager.getInstance().post(new NewMirrorDataEvent(mirrorData));
            } catch (FileContainsDuplicatesException e) {
                // do not sync changes from mirror if duplicates found in mirror
                System.out.println("Duplicate data found in mirror, cancelling sync...");
            }
        };

        int initialDelay = 0;
        scheduler.scheduleAtFixedRate(task, initialDelay, interval, TimeUnit.SECONDS);
    }

    private AddressBookWrapper getMirrorData() throws FileContainsDuplicatesException {
        System.out.println("Updating data from cloud: " + System.nanoTime());
        final File mirrorFile = new File(PreferencesManager.getInstance().getPersonFile().toString() + "-mirror.xml");
        final AddressBookWrapper data = cloudSimulator.getSimulatedCloudData(mirrorFile);
        if (data.containsDuplicates()) throw new FileContainsDuplicatesException();
        return data;
    }

    @Subscribe
    public void handleLocalModelChangedEvent(LocalModelChangedEvent lmce) {
        requestExecutor.execute(new CloudUpdateTask(this.cloudSimulator, lmce.personData, lmce.groupData));
    }
}
