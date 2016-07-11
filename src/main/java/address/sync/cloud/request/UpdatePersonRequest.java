package address.sync.cloud.request;

import address.exceptions.DataConversionException;
import address.sync.cloud.model.CloudAddressBook;
import address.sync.cloud.model.CloudPerson;
import address.sync.cloud.model.CloudTag;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

public class UpdatePersonRequest extends Request {
    private String addressBookName;
    private int personId;
    private CloudPerson updatedPerson;
    private CompletableFuture<CloudPerson> resultContainer;

    public UpdatePersonRequest(String addressBookName, int personId, CloudPerson updatedPerson, CompletableFuture<CloudPerson> resultContainer) {
        this.addressBookName = addressBookName;
        this.personId = personId;
        this.updatedPerson = updatedPerson;
        this.resultContainer = resultContainer;
    }

    @Override
    public void run() {
        try {
            CloudPerson returnedPerson = updatePersonInAddressBook(addressBookName, personId, updatedPerson);
            resultContainer.complete(returnedPerson);
        } catch (FileNotFoundException | DataConversionException | NoSuchElementException e) {
            resultContainer.completeExceptionally(e);
        }
    }

    private CloudPerson updatePersonInAddressBook(String addressBookName, int personId, CloudPerson updatedPerson) throws FileNotFoundException, DataConversionException {
        CloudAddressBook fileData = cloudFileHandler.readCloudAddressBook(addressBookName);
        CloudPerson returnedPerson = updatePersonDetails(fileData.getAllPersons(), fileData.getAllTags(), personId,
                updatedPerson);
        cloudFileHandler.writeCloudAddressBook(fileData);
        return returnedPerson;
    }

    private Optional<CloudPerson> getPerson(List<CloudPerson> personList, int personId) {
        return personList.stream()
                .filter(person -> person.getId() == personId)
                .findAny();
    }

    private CloudPerson updatePersonDetails(List<CloudPerson> personList, List<CloudTag> tagList, int personId,
                                            CloudPerson updatedPerson) throws NoSuchElementException {
        CloudPerson oldPerson = getPersonIfExists(personList, personId);
        oldPerson.updatedBy(updatedPerson);

        List<CloudTag> newTags = updatedPerson.getTags().stream()
                .filter(tag -> !tagList.contains(tag))
                .collect(Collectors.toCollection(ArrayList::new));
        tagList.addAll(newTags);

        return oldPerson;
    }

    private CloudPerson getPersonIfExists(List<CloudPerson> personList, int personId) {
        Optional<CloudPerson> personQueryResult = getPerson(personList, personId);
        if (!personQueryResult.isPresent()) throw new NoSuchElementException("No such person found.");

        return personQueryResult.get();
    }
}
