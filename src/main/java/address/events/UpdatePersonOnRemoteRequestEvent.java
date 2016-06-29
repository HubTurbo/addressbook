package address.events;

import address.model.datatypes.person.Person;

import java.util.concurrent.CompletableFuture;

public class UpdatePersonOnRemoteRequestEvent extends BaseEvent {
    private CompletableFuture<Person> returnedPersonContainer;
    private String addressBookName;
    private int personId;
    private Person updatedPerson;

    public UpdatePersonOnRemoteRequestEvent(CompletableFuture<Person> returnedPersonContainer, String addressBookName,
                                            int personId, Person updatedPerson) {
        this.returnedPersonContainer = returnedPersonContainer;
        this.addressBookName = addressBookName;
        this.personId = personId;
        this.updatedPerson = updatedPerson;
    }

    public CompletableFuture<Person> getReturnedPersonContainer() {
        return returnedPersonContainer;
    }

    public String getAddressBookName() {
        return addressBookName;
    }

    public int getPersonId() {
        return personId;
    }

    public Person getUpdatedPerson() {
        return updatedPerson;
    }

    @Override
    public String toString() {
        return "Request to update person #" + personId + " on remote " + addressBookName + " with: " + updatedPerson;
    }
}
