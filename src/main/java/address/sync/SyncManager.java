package address.sync;


import address.events.EventManager;
import address.events.NewMirrorData;
import address.model.Person;
import address.preferences.PreferencesManager;
import address.util.XmlHelper;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class SyncManager {

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    private List<Person> previousList = Collections.emptyList();

    public void startSyncingData(long interval){
        updatePeriodically(interval);
    }


    public void updatePeriodically(long interval) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            System.out.println("Updating data: " + System.nanoTime());
            File mirrorFile = null;
            List<Person> newData = Collections.emptyList();
            try {
                mirrorFile = new File(PreferencesManager.getInstance().getPersonFilePath().toString() + "-mirror.xml");
                newData = XmlHelper.getDataFromFile(mirrorFile);
            } catch (JAXBException e) {
                System.out.println("File not found or is not in valid xml format : " + mirrorFile);
            }

            if(!newData.isEmpty()) {
                EventManager.getInstance().post(new NewMirrorData(newData));
            }
        };

        int initialDelay = 0;
        executor.scheduleAtFixedRate(task, initialDelay, interval, TimeUnit.SECONDS);
    }
}
