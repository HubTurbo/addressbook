package address.events;

import address.model.datatypes.person.Person;

import java.util.concurrent.CompletableFuture;

public class CreatePersonOnRemoteRequestEvent extends BaseEvent {
    private CompletableFuture<Person> returnedPerson;
    private Person createdPerson;
    private String addressBookName;

    public CreatePersonOnRemoteRequestEvent(CompletableFuture<Person> returnedPerson, String addressBookName, Person createdPerson) {
        this.returnedPerson = returnedPerson;
        this.addressBookName = addressBookName;
        this.createdPerson = createdPerson;
    }

    public CompletableFuture<Person> getFutureContainer() {
        return returnedPerson;
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
