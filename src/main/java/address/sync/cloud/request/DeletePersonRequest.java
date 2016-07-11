package address.sync.cloud.request;

import address.exceptions.DataConversionException;
import address.sync.cloud.model.CloudAddressBook;
import address.sync.cloud.model.CloudPerson;
import address.util.AppLogger;
import address.util.LoggerManager;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;

public class DeletePersonRequest extends Request {
    private static AppLogger logger = LoggerManager.getLogger(DeletePersonRequest.class);
    private String addressBookName;
    private int personId;
    private CompletableFuture<Void> resultContainer;

    public DeletePersonRequest(String addressBookName, int personId, CompletableFuture<Void> resultContainer) {
        this.addressBookName = addressBookName;
        this.personId = personId;
        this.resultContainer = resultContainer;
    }

    @Override
    public void run() {
        try {
            logger.debug("Deleting person #{}", personId);
            deletePersonFromAddressBook(addressBookName, personId);
            logger.debug("Person #{} deleted", personId);
            resultContainer.complete(null);
        } catch (FileNotFoundException | DataConversionException | NoSuchElementException | RejectedExecutionException e) {
            logger.debug("Delete person request for #{} completed exceptionally: {}", personId, e);
            resultContainer.completeExceptionally(e);
        }
    }

    public void deletePersonFromAddressBook(String addressBookName, int personId) throws FileNotFoundException, DataConversionException, NoSuchElementException {
        CloudAddressBook fileData = cloudFileHandler.readCloudAddressBook(addressBookName);
        deletePersonFromData(fileData.getAllPersons(), personId);
        cloudFileHandler.writeCloudAddressBook(fileData);
    }

    private void deletePersonFromData(List<CloudPerson> personList, int personId) throws NoSuchElementException {
        CloudPerson deletedPerson = getPersonIfExists(personList, personId);
        deletedPerson.setDeleted(true);
    }

    private CloudPerson getPersonIfExists(List<CloudPerson> personList, int personId) throws NoSuchElementException {
        Optional<CloudPerson> personQueryResult = getPerson(personList, personId);
        if (!personQueryResult.isPresent()) throw new NoSuchElementException("No such person found.");

        return personQueryResult.get();
    }

    private Optional<CloudPerson> getPerson(List<CloudPerson> personList, int personId) {
        return personList.stream()
                .filter(person -> person.getId() == personId)
                .findAny();
    }
}
