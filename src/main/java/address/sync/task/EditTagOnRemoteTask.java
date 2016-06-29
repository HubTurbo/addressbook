package address.sync.task;

import address.exceptions.SyncErrorException;
import address.model.datatypes.tag.Tag;
import address.sync.RemoteManager;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;

public class EditTagOnRemoteTask implements Callable<Tag> {
    private RemoteManager remoteManager;
    private String addressBookName;
    private String tagName;
    private Tag editedTag;

    public EditTagOnRemoteTask(RemoteManager remoteManager, String addressBookName, String tagName, Tag editedTag) {
        this.remoteManager = remoteManager;
        this.addressBookName = addressBookName;
        this.tagName = tagName;
        this.editedTag = editedTag;
    }

    @Override
    public Tag call() throws Exception {
        try {
            Optional<Tag> editedTag = remoteManager.editTag(addressBookName, tagName, this.editedTag);
            if (!editedTag.isPresent()) throw new SyncErrorException("Error editing tag");
            return editedTag.get();
        } catch (IOException e) {
            throw new SyncErrorException("Error editing tag");
        }
    }
}
