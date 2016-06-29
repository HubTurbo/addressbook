package address.sync.task;

import address.exceptions.SyncErrorException;
import address.model.datatypes.tag.Tag;
import address.sync.RemoteManager;
import address.util.AppLogger;
import address.util.LoggerManager;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;

public class CreateTagOnRemoteTask implements Callable<Tag> {
    private static final AppLogger logger = LoggerManager.getLogger(CreateTagOnRemoteTask.class);
    private final RemoteManager remoteManager;
    private final String addressBookName;
    private final Tag tag;

    public CreateTagOnRemoteTask(RemoteManager remoteManager, String addressBookName, Tag tag) {
        this.remoteManager = remoteManager;
        this.addressBookName = addressBookName;
        this.tag = tag;
    }

    @Override
    public Tag call() throws Exception {
        logger.info("Creating {} in {} on remote", tag, addressBookName);
        try {
            Optional<Tag> createdTag = remoteManager.createTag(addressBookName, tag);
            if (!createdTag.isPresent()) throw new SyncErrorException("Error creating tag");
            return createdTag.get();
        } catch (IOException e) {
            throw new SyncErrorException("Error creating tag");
        }
    }
}
