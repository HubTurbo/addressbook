package address.events.sync;

import address.events.BaseEvent;
import address.model.datatypes.tag.Tag;

import java.util.concurrent.CompletableFuture;

public class CreateTagOnRemoteRequestEvent extends BaseEvent {
    private CompletableFuture<Tag> returnedTagContainer;
    private String addressBookName;
    private Tag createdTag;

    public CreateTagOnRemoteRequestEvent(CompletableFuture<Tag> returnedTagContainer, String addressBookName,
                                         Tag createdTag) {
        this.returnedTagContainer = returnedTagContainer;
        this.addressBookName = addressBookName;
        this.createdTag = createdTag;
    }

    public CompletableFuture<Tag> getReturnedTagContainer() {
        return returnedTagContainer;
    }

    public String getAddressBookName() {
        return addressBookName;
    }

    public Tag getCreatedTag() {
        return createdTag;
    }

    @Override
    public String toString() {
        return "Request to create tag on remote " + addressBookName + ": " + createdTag;
    }
}
