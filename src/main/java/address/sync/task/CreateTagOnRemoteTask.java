package address.sync.task;

import address.exceptions.SyncErrorException;
import address.model.datatypes.tag.Tag;
import address.sync.RemoteManager;
import address.util.AppLogger;
import address.util.LoggerManager;

import java.io.IOException;
import java.util.Optional;

public class CreateTagOnRemoteTask extends RemoteTaskWithResult<Tag> {
    private static final AppLogger logger = LoggerManager.getLogger(CreateTagOnRemoteTask.class);
    private final String addressBookName;
    private final Tag tag;

    public CreateTagOnRemoteTask(RemoteManager remoteManager, String addressBookName, Tag tag) {
        super(remoteManager);
        this.addressBookName = addressBookName;
        this.tag = tag;
    }

    @Override
    public Tag call() throws Exception {
        logger.info("Creating {} in {} on remote", tag, addressBookName);
        try {
            Optional<Tag> createdTag = remoteManager.createTag(addressBookName, tag);
            if (!createdTag.isPresent()) throw new SyncErrorException("Error creating tag " + tag);
            return createdTag.get();
        } catch (IOException e) {
            throw new SyncErrorException("Error creating tag " + tag);
        }
    }
}
