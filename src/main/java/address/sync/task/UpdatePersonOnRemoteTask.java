package address.sync.task;

import address.exceptions.SyncErrorException;
import address.model.datatypes.person.Person;
import address.sync.RemoteManager;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;

public class UpdatePersonOnRemoteTask implements Callable<Person> {
    private RemoteManager remoteManager;
    private String addressBookName;
    private int personId;
    private Person updatedPerson;

    public UpdatePersonOnRemoteTask(RemoteManager remoteManager, String addressBookName, int personId,
                                    Person updatedPerson) {
        this.remoteManager = remoteManager;
        this.addressBookName = addressBookName;
        this.personId = personId;
        this.updatedPerson = updatedPerson;
    }

    @Override
    public Person call() throws Exception {
        try {
            Optional<Person> updatedPerson = remoteManager.updatePerson(addressBookName, personId, this.updatedPerson);
            if (!updatedPerson.isPresent()) throw new SyncErrorException("Error updating person");
            return updatedPerson.get();
        } catch (IOException e) {
            throw new SyncErrorException("Error updating person");
        }
    }
}
