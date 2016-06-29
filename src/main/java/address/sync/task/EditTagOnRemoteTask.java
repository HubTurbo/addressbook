package address.sync.task;

import address.exceptions.SyncErrorException;
import address.model.datatypes.tag.Tag;
import address.sync.RemoteManager;
import address.util.AppLogger;
import address.util.LoggerManager;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;

public class EditTagOnRemoteTask implements Callable<Tag> {
    private static final AppLogger logger = LoggerManager.getLogger(EditTagOnRemoteTask.class);
    private final RemoteManager remoteManager;
    private final String addressBookName;
    private final String tagName;
    private final Tag editedTag;

    public EditTagOnRemoteTask(RemoteManager remoteManager, String addressBookName, String tagName, Tag editedTag) {
        this.remoteManager = remoteManager;
        this.addressBookName = addressBookName;
        this.tagName = tagName;
        this.editedTag = editedTag;
    }

    @Override
    public Tag call() throws Exception {
        logger.info("Editing tag {} with {} in {} on remote", tagName, editedTag, addressBookName);
        try {
            Optional<Tag> editedTag = remoteManager.editTag(addressBookName, tagName, this.editedTag);
            if (!editedTag.isPresent()) throw new SyncErrorException("Error editing tag");
            return editedTag.get();
        } catch (IOException e) {
            throw new SyncErrorException("Error editing tag");
        }
    }
}
