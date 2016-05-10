package address.sync;


import address.events.EventManager;
import address.events.LocalModelChangedEvent;
import address.events.NewMirrorDataEvent;
import address.model.Person;
import address.preferences.PreferencesManager;
import com.google.common.eventbus.Subscribe;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Syncs data between a mirror file and the primary data file
 */
public class SyncManager {

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    private List<Person> previousList = Collections.emptyList();

    private boolean isSimulatedRandomChanges = false;

    public SyncManager() {
        EventManager.getInstance().registerHandler(this);
    }

    public void startSyncingData(long interval, boolean isSimulateRandomChanges) {
        if(interval > 0) {
            this.isSimulatedRandomChanges = isSimulateRandomChanges;
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
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            List<Person> mirrorData = getMirrorData();

            if(!mirrorData.isEmpty()) {
                EventManager.getInstance().post(new NewMirrorDataEvent(mirrorData));
            }
        };

        int initialDelay = 0;
        executor.scheduleAtFixedRate(task, initialDelay, interval, TimeUnit.SECONDS);
    }

    private List<Person> getMirrorData() {
        System.out.println("Updating data: " + System.nanoTime());
        File mirrorFile = new File(PreferencesManager.getInstance().getPersonFilePath().toString() + "-mirror.xml");
        CloudSimulator cloudSimulator = new CloudSimulator(this.isSimulatedRandomChanges);
        return cloudSimulator.getSimulatedCloudData(mirrorFile);
    }

    @Subscribe
    public void handleLocalModelChangedEvent(LocalModelChangedEvent lmce) {
        System.out.println("Requesting changes to the cloud: " + System.nanoTime());
        File mirrorFile = new File(PreferencesManager.getInstance().getPersonFilePath().toString() + "-mirror.xml");
        CloudSimulator cloudSimulator = new CloudSimulator(this.isSimulatedRandomChanges);
        try {
            cloudSimulator.requestChangesToCloud(mirrorFile, lmce.personData, 2);
        } catch (JAXBException e) {
            System.out.println("Error requesting changes to the cloud");
        }
    }
}
