package address.sync.task;

import address.exceptions.SyncErrorException;
import address.model.datatypes.person.Person;
import address.sync.RemoteManager;
import address.util.AppLogger;
import address.util.LoggerManager;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;

public class UpdatePersonOnRemoteTask implements Callable<Person> {
    private static final AppLogger logger = LoggerManager.getLogger(UpdatePersonOnRemoteTask.class);
    private final RemoteManager remoteManager;
    private final String addressBookName;
    private final int personId;
    private final Person updatedPerson;

    public UpdatePersonOnRemoteTask(RemoteManager remoteManager, String addressBookName, int personId,
                                    Person updatedPerson) {
        this.remoteManager = remoteManager;
        this.addressBookName = addressBookName;
        this.personId = personId;
        this.updatedPerson = updatedPerson;
    }

    @Override
    public Person call() throws Exception {
        logger.info("Updating person id {} with person {} in {} on remote", personId, updatedPerson, addressBookName);
        try {
            Optional<Person> updatedPerson = remoteManager.updatePerson(addressBookName, personId, this.updatedPerson);
            if (!updatedPerson.isPresent()) throw new SyncErrorException("Error updating person");
            return updatedPerson.get();
        } catch (IOException e) {
            throw new SyncErrorException("Error updating person");
        }
    }
}
