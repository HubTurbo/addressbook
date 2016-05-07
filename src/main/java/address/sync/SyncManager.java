package address.sync;


import address.events.EventManager;
import address.events.NewDataEvent;
import address.model.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class SyncManager {

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public void startSyncingData(long interval){
        updatePeriodically(interval);
    }


    public void updatePeriodically(long interval) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            System.out.println("Updating data: " + System.nanoTime());
            List<Person> newData = new ArrayList<>();
            newData.add(new Person("Mr "+System.nanoTime(), ""));
            EventManager.getInstance().post(new NewDataEvent(newData));
        };

        int initialDelay = 0;
        executor.scheduleAtFixedRate(task, initialDelay, interval, TimeUnit.SECONDS);
    }
}
