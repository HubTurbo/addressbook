package address.events.sync;

import address.events.BaseEvent;
import address.model.datatypes.tag.Tag;

import java.util.concurrent.CompletableFuture;

public class EditTagOnRemoteRequestEvent extends BaseEvent {
    private CompletableFuture<Tag> returnedTagContainer;
    private String addressBookName;
    private String tagName;
    private Tag editedTag;

    public EditTagOnRemoteRequestEvent(CompletableFuture<Tag> returnedTagContainer, String addressBookName,
                                       String tagName, Tag editedTag) {
        this.returnedTagContainer = returnedTagContainer;
        this.addressBookName = addressBookName;
        this.tagName = tagName;
        this.editedTag = editedTag;
    }

    public CompletableFuture<Tag> getReturnedTagContainer() {
        return returnedTagContainer;
    }

    public String getAddressBookName() {
        return addressBookName;
    }

    public String getTagName() {
        return tagName;
    }

    public Tag getEditedTag() {
        return editedTag;
    }

    @Override
    public String toString() {
        return "Request to edit tag " + tagName + " on remote " + addressBookName + " with: " + editedTag;
    }
}
