package address.sync.task;

import address.exceptions.SyncErrorException;
import address.model.datatypes.person.Person;
import address.sync.RemoteManager;
import address.util.AppLogger;
import address.util.LoggerManager;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;

public class CreatePersonOnRemoteTask implements Callable<Person> {
    private static final AppLogger logger = LoggerManager.getLogger(CreatePersonOnRemoteTask.class);
    private final RemoteManager remoteManager;
    private final String addressBookName;
    private final Person person;

    public CreatePersonOnRemoteTask(RemoteManager remoteManager, String addressBookName, Person person) {
        this.remoteManager = remoteManager;
        this.addressBookName = addressBookName;
        this.person = person;
    }

    @Override
    public Person call() throws Exception {
        logger.info("Attempting to create {} on the remote", person);
        try {
            Optional<Person> createdPerson = remoteManager.createPerson(addressBookName, person);
            if (!createdPerson.isPresent()) throw new SyncErrorException("Error creating person");
            return createdPerson.get();
        } catch (IOException e) {
            throw new SyncErrorException("Error creating person");
        }
    }
}
