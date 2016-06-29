package address.events;

import address.model.datatypes.tag.Tag;

import java.util.concurrent.CompletableFuture;

public class CreateTagOnRemoteRequestEvent extends BaseEvent {
    private CompletableFuture<Tag> returnedTag;
    private String addressBookName;
    private Tag createdTag;

    public CreateTagOnRemoteRequestEvent(CompletableFuture<Tag> returnedTag, String addressBookName, Tag createdTag) {
        this.returnedTag = returnedTag;
        this.addressBookName = addressBookName;
        this.createdTag = createdTag;
    }

    public CompletableFuture<Tag> getFutureContainer() {
        return returnedTag;
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
