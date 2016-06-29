package address.sync.task;

import address.sync.RemoteManager;
import address.util.AppLogger;
import address.util.LoggerManager;

import java.util.concurrent.Callable;

public class DeletePersonOnRemoteTask implements Callable<Boolean> {
    private static final AppLogger logger = LoggerManager.getLogger(DeletePersonOnRemoteTask.class);
    private final RemoteManager remoteManager;
    private final String addressBookName;
    private final int personId;

    public DeletePersonOnRemoteTask(RemoteManager remoteManager, String addressBookName, int personId) {
        this.remoteManager = remoteManager;
        this.addressBookName = addressBookName;
        this.personId = personId;
    }

    @Override
    public Boolean call() throws Exception {
        logger.info("Deleting person {} from {} on remote", personId, addressBookName);
        return remoteManager.deletePerson(addressBookName, personId);
    }
}