package address.sync.task;

import address.model.ContactGroup;
import address.model.Person;
import address.preferences.PreferencesManager;
import address.sync.CloudSimulator;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.List;

public class CloudUpdateTask implements Runnable {
    private final CloudSimulator simulator;
    private final List<Person> personsData;
    private final List<ContactGroup> groupsData;

    public CloudUpdateTask(CloudSimulator simulator, List<Person> personsData, List<ContactGroup> groupsData) {
        this.simulator = simulator;
        this.personsData = personsData;
        this.groupsData = groupsData;
    }

    @Override
    public void run() {
        System.out.println("Requesting changes to the cloud: " + System.nanoTime());
        File mirrorFile = new File(PreferencesManager.getInstance().getPersonFile().toString() + "-mirror.xml");
        try {
            simulator.requestChangesToCloud(mirrorFile, this.personsData, this.groupsData, 3);
        } catch (JAXBException e) {
            System.out.println("Error requesting changes to the cloud");
        }
    }
}
