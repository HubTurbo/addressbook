package address.sync.cloud.request;

import address.exceptions.DataConversionException;
import address.sync.cloud.model.CloudAddressBook;
import address.sync.cloud.model.CloudPerson;
import address.sync.cloud.model.CloudTag;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;

public class EditTagRequest extends Request {
    private String addressBookName;
    private String oldTagName;
    private CloudTag editedTag;
    private CompletableFuture<CloudTag> resultContainer;

    public EditTagRequest(String addressBookName, String oldTagName, CloudTag editedTag, CompletableFuture<CloudTag> resultContainer) {
        this.addressBookName = addressBookName;
        this.oldTagName = oldTagName;
        this.editedTag = editedTag;
        this.resultContainer = resultContainer;
    }
    @Override
    public void run() {
        try {
            cloudRateLimitStatus.useQuota();
            CloudTag returnedTag = editTagInAddressBook(addressBookName, oldTagName, editedTag);
            resultContainer.complete(returnedTag);
        } catch (FileNotFoundException | DataConversionException | RejectedExecutionException e) {
            resultContainer.completeExceptionally(e);
        }
    }

    private CloudTag editTagInAddressBook(String addressBookName, String oldTagName, CloudTag editedTag) throws FileNotFoundException, DataConversionException {
        CloudAddressBook fileData = cloudFileHandler.readCloudAddressBook(addressBookName);
        CloudTag returnedTag = updateTagDetails(fileData.getAllPersons(), fileData.getAllTags(), oldTagName,
                editedTag);
        cloudFileHandler.writeCloudAddressBook(fileData);
        return returnedTag;
    }

    private CloudTag updateTagDetails(List<CloudPerson> personList, List<CloudTag> tagList, String oldTagName,
                                      CloudTag updatedTag) throws NoSuchElementException {
        CloudTag oldTag = getTagIfExists(tagList, oldTagName);
        oldTag.updatedBy(updatedTag);
        personList.stream()
                .forEach(person -> {
                    List<CloudTag> personTags = person.getTags();
                    personTags.stream()
                            .filter(personTag -> personTag.getName().equals(oldTagName))
                            .forEach(personTag -> personTag.updatedBy(updatedTag));
                });
        return oldTag;
    }

    private CloudTag getTagIfExists(List<CloudTag> tagList, String tagName) {
        Optional<CloudTag> tagQueryResult = getTag(tagList, tagName);
        if (!tagQueryResult.isPresent()) throw new NoSuchElementException("No such tag found.");

        return tagQueryResult.get();
    }

    private Optional<CloudTag> getTag(List<CloudTag> tagList, String tagName) {
        return tagList.stream()
                .filter(tag -> tag.getName().equals(tagName))
                .findAny();
    }
}
