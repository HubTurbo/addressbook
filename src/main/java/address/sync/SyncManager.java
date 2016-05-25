package address.sync;


import address.controller.MainController;
import address.events.EventManager;
import address.events.LocalModelChangedEvent;
import address.events.NewMirrorDataEvent;
import address.events.SaveRequestEvent;
import address.exceptions.FileContainsDuplicatesException;
import address.model.AddressBook;
import address.prefs.PrefsManager;
import address.sync.task.CloudUpdateTask;
import com.google.common.eventbus.Subscribe;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

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
                DoubleProperty progress = new SimpleDoubleProperty();
                MainController.syncStatusBar.displayMessage("Sync Started");
                MainController.syncStatusBar.progressProperty().bind(progress);
                MainController.syncStatusBar.displayMessage("Sync in progress");
                Optional<AddressBook> mirrorData = getMirrorData(progress);
                if (!mirrorData.isPresent()) {
                    MainController.syncStatusBar.displayMessage(
                            "Unable to retrieve data from mirror, cancelling sync...");
                    System.out.println("Unable to retrieve data from mirror, cancelling sync...");
                    return;
                }
                EventManager.getInstance().post(new NewMirrorDataEvent(mirrorData.get()));
                MainController.syncStatusBar.displayMessage("Sync finished");
            } catch (FileContainsDuplicatesException e) {
                // do not sync changes from mirror if duplicates found in mirror
                MainController.syncStatusBar.displayMessage(
                        "Duplicate data found in mirror, cancelling sync...");
                System.out.println("Duplicate data found in mirror, cancelling sync...");
            }
        };

        long initialDelay = 300; // temp fix for issue #66
        scheduler.scheduleWithFixedDelay(task, initialDelay, interval, TimeUnit.MILLISECONDS);
    }

    private Optional<AddressBook> getMirrorData(DoubleProperty progress) throws FileContainsDuplicatesException {
        System.out.println("Updating data from cloud: " + System.nanoTime());
        final File mirrorFile = PrefsManager.getInstance().getMirrorLocation();
        final Optional<AddressBook> data = cloudSimulator.getSimulatedCloudData(mirrorFile, progress);
        if (data.isPresent() && data.get().containsDuplicates()) throw new FileContainsDuplicatesException(mirrorFile);
        return data;
    }

    @Subscribe
    public void handleLocalModelChangedEvent(LocalModelChangedEvent lmce) {
        requestExecutor.execute(new CloudUpdateTask(this.cloudSimulator, lmce.personData, lmce.groupData));
    }

    // To be removed after working out specification on saving and syncing behaviour
    @Subscribe
    public void handleSaveRequestEvent(SaveRequestEvent sre) {
        requestExecutor.execute(new CloudUpdateTask(this.cloudSimulator, sre.personData, sre.groupData));
    }
}
