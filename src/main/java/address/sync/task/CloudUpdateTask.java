package address.sync.task;

import address.model.datatypes.ContactGroup;
import address.model.datatypes.Person;
import address.model.datatypes.UniqueData;
import address.prefs.PrefsManager;
import address.sync.CloudService;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CloudUpdateTask implements Runnable {
    private final CloudService simulator;
    private final List<Person> personsData;
    private final List<ContactGroup> groupsData;

    public CloudUpdateTask(CloudService simulator, List<Person> personsData,
                           List<ContactGroup> groupsData) {
        this.simulator = simulator;
        this.personsData = personsData;
        this.groupsData = groupsData;
    }

    @Override
    public void run() {
        System.out.println("Requesting changes to the cloud: " + System.nanoTime());

        List<UniqueData> allData = new ArrayList<>();
        allData.addAll(personsData);
        allData.addAll(groupsData);

        File mirrorFile = PrefsManager.getInstance().getMirrorLocation();
//        try {
//            simulator.requestChangesToCloud(mirrorFile, personsData, groupsData);
//        } catch (JAXBException e) {
//            System.out.println("Error requesting changes to the cloud");
//        }
    }
}
