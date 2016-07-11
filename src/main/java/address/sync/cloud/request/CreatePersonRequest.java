package address.sync.cloud.request;

import address.exceptions.DataConversionException;
import address.sync.cloud.model.CloudAddressBook;
import address.sync.cloud.model.CloudPerson;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;

public class CreatePersonRequest extends Request {
    private String addressBookName;
    private CloudPerson newPerson;
    private CompletableFuture<CloudPerson> resultContainer;

    public CreatePersonRequest(String addressBookName, CloudPerson newPerson, CompletableFuture<CloudPerson> resultContainer) {
        this.addressBookName = addressBookName;
        this.newPerson = newPerson;
        this.resultContainer = resultContainer;
    }

    @Override
    public void run() {
        try {
            CloudPerson returnedPerson = createPersonInAddressBook(addressBookName, newPerson);
            resultContainer.complete(returnedPerson);
        } catch (FileNotFoundException | DataConversionException | IllegalArgumentException e) {
            resultContainer.completeExceptionally(e);
        }
    }

    private CloudPerson createPersonInAddressBook(String addressBookName, CloudPerson newPerson) throws FileNotFoundException, DataConversionException {
        CloudAddressBook fileData = cloudFileHandler.readCloudAddressBook(addressBookName);
        CloudPerson returnedPerson = addPerson(fileData.getAllPersons(), newPerson);
        cloudFileHandler.writeCloudAddressBook(fileData);
        return returnedPerson;
    }

    /**
     * Verifies whether newPerson can be added, and adds it to the persons list
     *
     * @param personList
     * @param newPerson
     * @return newPerson, if added successfully
     */
    private CloudPerson addPerson(List<CloudPerson> personList, CloudPerson newPerson)
            throws IllegalArgumentException {
        if (newPerson == null) throw new IllegalArgumentException("Person cannot be null");
        if (!newPerson.isValid()) throw new IllegalArgumentException("Invalid person");

        CloudPerson personToAdd = generateIdForPerson(personList, newPerson);
        personList.add(personToAdd);

        return personToAdd;
    }

    private CloudPerson generateIdForPerson(List<CloudPerson> personList, CloudPerson newPerson) {
        newPerson.setId(personList.size() + 1);
        return newPerson;
    }
}
