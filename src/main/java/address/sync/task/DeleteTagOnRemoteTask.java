package address.sync.task;

import address.exceptions.SyncErrorException;
import address.sync.RemoteManager;
import address.util.AppLogger;
import address.util.LoggerManager;

import java.io.IOException;
import java.util.concurrent.Callable;

public class DeleteTagOnRemoteTask implements Callable<Boolean> {
    private static final AppLogger logger = LoggerManager.getLogger(DeleteTagOnRemoteTask.class);
    private final RemoteManager remoteManager;
    private final String addressBookName;
    private final String tagName;

    public DeleteTagOnRemoteTask(RemoteManager remoteManager, String addressBookName, String tagName) {
        this.remoteManager = remoteManager;
        this.addressBookName = addressBookName;
        this.tagName = tagName;
    }

    @Override
    public Boolean call() throws Exception {
        try {
            logger.info("Deleting tag {} from {} on remote", tagName, addressBookName);
            return remoteManager.deleteTag(addressBookName, tagName);
        } catch (IOException e) {
            throw new SyncErrorException("Error deleting tag " + tagName + " from " + addressBookName + " on remote");
        }
    }
}
