package address.sync;


import address.events.EventManager;
import address.events.LocalModelChangedEvent;
import address.events.NewMirrorDataEvent;
import address.model.AddressBookWrapper;
import address.model.Person;
import address.preferences.PreferencesManager;
import address.sync.task.CloudUpdateTask;
import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.util.Collections;
import java.util.List;
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

    private List<Person> previousList = Collections.emptyList();

    public SyncManager() {
        EventManager.getInstance().registerHandler(this);
    }

    public void startSyncingData(long interval, boolean isSimulateRandomChanges) {
        if(interval > 0) {
            this.cloudSimulator = new CloudSimulator(isSimulateRandomChanges);
            updatePeriodically(interval);
        }
    }


    /**
     * Runs periodically and adds any entries in the mirror file that is missing
     * in the primary data file. The mirror file should be at the same location
     * as primary file and the name should be '{primary file name}-mirror.xml'.
     * @param interval The period between updates
     */
    public void updatePeriodically(long interval) {
        Runnable task = () -> {
            final AddressBookWrapper mirrorData = getMirrorData();

            if (!mirrorData.getPersons().isEmpty() || !mirrorData.getGroups().isEmpty()) {
                EventManager.getInstance().post(new NewMirrorDataEvent(mirrorData));
            }
        };

        int initialDelay = 0;
        scheduler.scheduleAtFixedRate(task, initialDelay, interval, TimeUnit.SECONDS);
    }

    private AddressBookWrapper getMirrorData() {
        System.out.println("Updating data: " + System.nanoTime());
        File mirrorFile = new File(PreferencesManager.getInstance().getPersonFile().toString() + "-mirror.xml");
        return this.cloudSimulator.getSimulatedCloudData(mirrorFile);
    }

    @Subscribe
    public void handleLocalModelChangedEvent(LocalModelChangedEvent lmce) {
        requestExecutor.execute(new CloudUpdateTask(this.cloudSimulator, lmce.personData, lmce.groupData));
    }
}
