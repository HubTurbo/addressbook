package address.sync.task;

import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;
import address.model.datatypes.UniqueData;
import address.prefs.PrefsManager;
import address.sync.CloudService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CloudUpdateTask implements Runnable {
    private final CloudService simulator;
    private final List<Person> personsData;
    private final List<Tag> tagsData;

    public CloudUpdateTask(CloudService simulator, List<Person> personsData,
                           List<Tag> tagsData) {
        this.simulator = simulator;
        this.personsData = personsData;
        this.tagsData = tagsData;
    }

    @Override
    public void run() {
        System.out.println("Requesting changes to the cloud: " + System.nanoTime());

        List<UniqueData> allData = new ArrayList<>();
        allData.addAll(personsData);
        allData.addAll(tagsData);

        File mirrorFile = PrefsManager.getInstance().getMirrorLocation();

        // Temporarily disabled
        // The way the local model should use the API to push its updates is not yet finalized
        /*try {
            simulator.requestChangesToCloud(mirrorFile, personsData, tagsData);
        } catch (JAXBException e) {
            System.out.println("Error requesting changes to the cloud");
        }*/
    }
}
