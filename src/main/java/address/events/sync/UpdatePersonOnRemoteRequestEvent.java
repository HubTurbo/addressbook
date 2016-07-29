package address.events.sync;

import address.events.BaseEvent;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;

import java.util.concurrent.CompletableFuture;

public class UpdatePersonOnRemoteRequestEvent extends BaseEvent {
    private CompletableFuture<ReadOnlyPerson> returnedPersonContainer;
    private String addressBookName;
    private int personId;
    private ReadOnlyPerson updatedPerson;

    public UpdatePersonOnRemoteRequestEvent(CompletableFuture<ReadOnlyPerson> returnedPersonContainer,
                                            String addressBookName, int personId, ReadOnlyPerson updatedPerson) {
        this.returnedPersonContainer = returnedPersonContainer;
        this.addressBookName = addressBookName;
        this.personId = personId;
        this.updatedPerson = updatedPerson;
    }

    public CompletableFuture<ReadOnlyPerson> getReturnedPersonContainer() {
        return returnedPersonContainer;
    }

    public String getAddressBookName() {
        return addressBookName;
    }

    public int getPersonId() {
        return personId;
    }

    public ReadOnlyPerson getUpdatedPerson() {
        return updatedPerson;
    }

    @Override
    public String toString() {
        return "Request to update person #" + personId + " on remote " + addressBookName + " with: " + updatedPerson;
    }
}
