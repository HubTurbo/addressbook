package address.sync.task;

import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;
import address.model.datatypes.UniqueData;
import address.sync.RemoteService;
import address.util.AppLogger;
import address.util.LoggerManager;

import java.util.ArrayList;
import java.util.List;

public class CloudUpdateTask implements Runnable {
    private static final AppLogger logger = LoggerManager.getLogger(CloudUpdateTask.class);
    private final RemoteService simulator;
    private final List<Person> personsData;
    private final List<Tag> tagsData;

    public CloudUpdateTask(RemoteService simulator, List<Person> personsData,
                           List<Tag> tagsData) {
        this.simulator = simulator;
        this.personsData = personsData;
        this.tagsData = tagsData;
    }

    @Override
    public void run() {
        logger.info("Requesting changes to the cloud.");

        List<UniqueData> allData = new ArrayList<>();
        allData.addAll(personsData);
        allData.addAll(tagsData);

        // TODO: Determine what calls should be made
    }
}
