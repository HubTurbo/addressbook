package address.events;

import address.model.datatypes.person.Person;

import java.util.concurrent.CompletableFuture;

public class CreatePersonOnRemoteRequestEvent extends BaseEvent {
    private CompletableFuture<Person> returnedPersonContainer;
    private Person createdPerson;
    private String addressBookName;

    public CreatePersonOnRemoteRequestEvent(CompletableFuture<Person> returnedPersonContainer, String addressBookName,
                                            Person createdPerson) {
        this.returnedPersonContainer = returnedPersonContainer;
        this.addressBookName = addressBookName;
        this.createdPerson = createdPerson;
    }

    public CompletableFuture<Person> getReturnedPersonContainer() {
        return returnedPersonContainer;
    }

    public Person getCreatedPerson() {
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
