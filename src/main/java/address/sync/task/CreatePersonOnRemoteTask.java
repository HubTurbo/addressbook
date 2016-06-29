package address.sync.task;

import address.exceptions.SyncErrorException;
import address.model.datatypes.person.Person;
import address.sync.RemoteManager;
import address.util.AppLogger;
import address.util.LoggerManager;

import java.io.IOException;
import java.util.Optional;

public class CreatePersonOnRemoteTask extends RemoteTaskWithResult<Person> {
    private static final AppLogger logger = LoggerManager.getLogger(CreatePersonOnRemoteTask.class);
    private final String addressBookName;
    private final Person person;

    public CreatePersonOnRemoteTask(RemoteManager remoteManager, String addressBookName, Person person) {
        super(remoteManager);
        this.addressBookName = addressBookName;
        this.person = person;
    }

    @Override
    public Person call() throws Exception {
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
