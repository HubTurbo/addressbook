package address.sync.task;

import address.model.Person;
import address.preferences.PreferencesManager;
import address.sync.CloudSimulator;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.List;

public class CloudUpdateTask implements Runnable {
    private final CloudSimulator simulator;
    private final List<Person> data;

    public CloudUpdateTask(CloudSimulator simulator, List<Person> data) {
        this.simulator = simulator;
        this.data = data;
    }

    @Override
    public void run() {
        System.out.println("Requesting changes to the cloud: " + System.nanoTime());
        File mirrorFile = new File(PreferencesManager.getInstance().getPersonFilePath().toString() + "-mirror.xml");
        try {
            simulator.requestChangesToCloud(mirrorFile, this.data, 3);
        } catch (JAXBException e) {
            System.out.println("Error requesting changes to the cloud");
        }
    }
}
