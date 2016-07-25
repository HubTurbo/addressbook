package address.events.sync;

import address.events.BaseEvent;

import java.util.concurrent.CompletableFuture;

public class DeletePersonOnRemoteRequestEvent extends BaseEvent {
    private CompletableFuture<Boolean> resultContainer;
    private String addressBookName;
    private int personId;

    public DeletePersonOnRemoteRequestEvent(CompletableFuture<Boolean> resultContainer, String addressBookName,
                                            int personId) {
        this.resultContainer = resultContainer;
        this.addressBookName = addressBookName;
        this.personId = personId;
    }

    public CompletableFuture<Boolean> getResultContainer() {
        return resultContainer;
    }

    public String getAddressBookName() {
        return addressBookName;
    }

    public int getPersonId() {
        return personId;
    }

    @Override
    public String toString() {
        return "Request to delete person #" + personId + " on remote " + addressBookName;
    }
}
