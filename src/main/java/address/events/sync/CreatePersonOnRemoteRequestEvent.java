package address.events.sync;

import address.events.BaseEvent;
import address.model.datatypes.person.ReadOnlyPerson;

import java.util.concurrent.CompletableFuture;

public class CreatePersonOnRemoteRequestEvent extends BaseEvent {
    private CompletableFuture<ReadOnlyPerson> returnedPersonContainer;
    private ReadOnlyPerson createdPerson;
    private String addressBookName;

    public CreatePersonOnRemoteRequestEvent(CompletableFuture<ReadOnlyPerson> returnedPersonContainer, String addressBookName,
                                            ReadOnlyPerson createdPerson) {
        this.returnedPersonContainer = returnedPersonContainer;
        this.addressBookName = addressBookName;
        this.createdPerson = createdPerson;
    }

    public CompletableFuture<ReadOnlyPerson> getReturnedPersonContainer() {
        return returnedPersonContainer;
    }

    public ReadOnlyPerson getCreatedPerson() {
        return createdPerson;
    }

    public String getAddressBookName() {
        return addressBookName;
    }

    @Override
    public String toString() {
        return "Request to create person on remote " + addressBookName + ": " + createdPerson;
    }
}
