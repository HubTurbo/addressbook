package address.sync.task;

import address.model.datatypes.ReadOnlyAddressBook;
import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;
import address.model.datatypes.UniqueData;
import address.sync.RemoteManager;
import address.sync.RemoteService;
import address.util.AppLogger;
import address.util.LoggerManager;

import java.util.ArrayList;
import java.util.List;

public class CloudUpdateTask implements Runnable {
    private static final AppLogger logger = LoggerManager.getLogger(CloudUpdateTask.class);
    private final RemoteManager remoteManager;
    private final ReadOnlyAddressBook data;

    public CloudUpdateTask(RemoteManager remoteManager, ReadOnlyAddressBook data) {
        this.remoteManager = remoteManager;
        this.data = data;
    }

    @Override
    public void run() {
        logger.info("Requesting changes to the cloud. (INCOMPLETE IMPLEMENTATION)");
        // TODO: Determine what calls should be made
    }
}
