package address.sync.task;

import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;
import address.model.datatypes.UniqueData;
import address.prefs.PrefsManager;
import address.sync.CloudService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CloudUpdateTask implements Runnable {
    private static final Logger logger = LogManager.getLogger(CloudUpdateTask.class);
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
        logger.info("Requesting changes to the cloud: " + System.nanoTime());

        List<UniqueData> allData = new ArrayList<>();
        allData.addAll(personsData);
        allData.addAll(tagsData);

        // TODO: Determine what calls should be made
    }
}
