package address.sync.task;

import address.events.CloudChangeResultReturnedEvent;
import address.events.EventManager;
import address.model.*;
import address.preferences.PreferencesManager;
import address.sync.CloudSimulator;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.List;

public class CloudUpdateTask implements Runnable {
    private final CloudSimulator simulator;
    private final List<ModelPerson> personsData;
    private final List<ModelContactGroup> groupsData;

    public CloudUpdateTask(CloudSimulator simulator, List<ModelPerson> personsData,
                           List<ModelContactGroup> groupsData) {
        this.simulator = simulator;
        this.personsData = personsData;
        this.groupsData = groupsData;
    }

    @Override
    public void run() {
        System.out.println("Requesting changes to the cloud: " + System.nanoTime());
        File mirrorFile = new File(PreferencesManager.getInstance().getPersonFile().toString() + "-mirror.xml");
        try {
            simulator.requestChangesToCloud(mirrorFile, ModelManager.convertToPersons(this.personsData),
                                            ModelManager.convertToGroups(this.groupsData), 3);
            EventManager.getInstance().post(new CloudChangeResultReturnedEvent(
                    CloudChangeResultReturnedEvent.Result.EDIT, this.personsData, true));
        } catch (JAXBException e) {
            System.out.println("Error requesting changes to the cloud");
            EventManager.getInstance().post(new CloudChangeResultReturnedEvent(
                    CloudChangeResultReturnedEvent.Result.EDIT, this.personsData, false));
        }
    }
}
