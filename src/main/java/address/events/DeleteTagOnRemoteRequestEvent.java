package address.events;

import java.util.concurrent.CompletableFuture;

public class DeleteTagOnRemoteRequestEvent extends BaseEvent {
    private CompletableFuture<Boolean> resultContainer;
    private String addressBookName;
    private String tagName;

    public DeleteTagOnRemoteRequestEvent(CompletableFuture<Boolean> resultContainer, String addressBookName,
                                            String tagName) {
        this.resultContainer = resultContainer;
        this.addressBookName = addressBookName;
        this.tagName = tagName;
    }

    public CompletableFuture<Boolean> getResultContainer() {
        return resultContainer;
    }

    public String getAddressBookName() {
        return addressBookName;
    }

    public String getTagName() {
        return tagName;
    }

    @Override
    public String toString() {
        return "Request to delete tag " + tagName + " on remote " + addressBookName;
    }
}
