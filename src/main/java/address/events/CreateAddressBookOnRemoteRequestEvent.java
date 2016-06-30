package address.events;

import java.util.concurrent.CompletableFuture;

public class CreateAddressBookOnRemoteRequestEvent extends BaseEvent {
    private CompletableFuture<Boolean> resultContainer;
    private String addressBookName;

    public CreateAddressBookOnRemoteRequestEvent(CompletableFuture<Boolean> resultContainer, String addressBookName) {
        this.resultContainer = resultContainer;
        this.addressBookName = addressBookName;
    }

    public CompletableFuture<Boolean> getResultContainer() {
        return resultContainer;
    }

    public String getAddressBookName() {
        return addressBookName;
    }

    @Override
    public String toString() {
        return "Request to create addressbook " + addressBookName + " on remote";
    }
}
