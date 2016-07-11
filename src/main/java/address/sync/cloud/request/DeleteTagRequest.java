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
import java.util.stream.Collectors;

public class DeleteTagRequest extends Request {
    private String addressBookName;
    private String tagName;
    private CompletableFuture<Void> resultContainer;

    public DeleteTagRequest(String addressBookName, String tagName, CompletableFuture<Void> resultContainer) {
        this.addressBookName = addressBookName;
        this.tagName = tagName;
        this.resultContainer = resultContainer;
    }

    public void run() {
        try {
            deleteTagFromAddressBook(addressBookName, tagName);
            resultContainer.complete(null);
        } catch (FileNotFoundException | DataConversionException | NoSuchElementException | RejectedExecutionException e) {
            resultContainer.completeExceptionally(e);
        }
    }

    private void deleteTagFromAddressBook(String addressBookName, String tagName) throws FileNotFoundException, DataConversionException {
        CloudAddressBook fileData = cloudFileHandler.readCloudAddressBook(addressBookName);
        deleteTagFromData(fileData.getAllPersons(), fileData.getAllTags(), tagName);
        cloudFileHandler.writeCloudAddressBook(fileData);
    }

    private void deleteTagFromData(List<CloudPerson> personList, List<CloudTag> tagList, String tagName)
            throws NoSuchElementException {
        CloudTag tag = getTagIfExists(tagList, tagName);
        // This may differ from how GitHub does it, but we won't know for sure
        tagList.remove(tag);
        personList.stream()
                .forEach(person -> {
                    List<CloudTag> personTags = person.getTags();
                    personTags = personTags.stream()
                            .filter(personTag -> !personTag.getName().equals(tagName))
                            .collect(Collectors.toList());
                    person.setTags(personTags);
                });
    }

    private CloudTag getTagIfExists(List<CloudTag> tagList, String tagName) throws NoSuchElementException {
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
