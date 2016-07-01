package address.sync.task;

import address.exceptions.SyncErrorException;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.sync.RemoteManager;
import address.util.AppLogger;
import address.util.LoggerManager;

import java.io.IOException;
import java.util.Optional;

public class CreatePersonOnRemoteTask extends RemoteTaskWithResult<ReadOnlyPerson> {
    private static final AppLogger logger = LoggerManager.getLogger(CreatePersonOnRemoteTask.class);
    private final String addressBookName;
    private final ReadOnlyPerson person;

    public CreatePersonOnRemoteTask(RemoteManager remoteManager, String addressBookName, ReadOnlyPerson person) {
        super(remoteManager);
        this.addressBookName = addressBookName;
        this.person = person;
    }

    @Override
    public Person call() throws SyncErrorException {
        logger.info("Creating {} in {} on remote", person, addressBookName);
        try {
            Optional<Person> createdPerson = remoteManager.createPerson(addressBookName, person);
            if (!createdPerson.isPresent()) throw new SyncErrorException("Error creating person " + person);
            return createdPerson.get();
        } catch (IOException e) {
            throw new SyncErrorException("Error creating person" + person);
        }
    }
}
