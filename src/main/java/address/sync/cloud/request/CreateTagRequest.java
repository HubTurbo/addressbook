package address.sync.cloud.request;

import address.exceptions.DataConversionException;
import address.sync.cloud.model.CloudAddressBook;
import address.sync.cloud.model.CloudTag;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;

public class CreateTagRequest extends Request {
    private String addressBookName;
    private CloudTag newTag;
    private CompletableFuture<CloudTag> resultContainer;

    public CreateTagRequest(String addressBookName, CloudTag newTag, CompletableFuture<CloudTag> resultContainer) {
        this.addressBookName = addressBookName;
        this.newTag = newTag;
        this.resultContainer = resultContainer;
    }

    @Override
    public void run() {
        try {
            CloudTag returnedTag = createTagInAddressBook(addressBookName, newTag);
            resultContainer.complete(returnedTag);
        } catch (FileNotFoundException | DataConversionException | RejectedExecutionException e) {
            resultContainer.completeExceptionally(e);
        }
    }

    private CloudTag createTagInAddressBook(String addressBookName, CloudTag newTag) throws FileNotFoundException, DataConversionException {
        CloudAddressBook fileData = cloudFileHandler.readCloudAddressBook(addressBookName);
        CloudTag returnedTag = addTag(fileData.getAllTags(), newTag);
        cloudFileHandler.writeCloudAddressBook(fileData);
        return returnedTag;
    }

    private CloudTag addTag(List<CloudTag> tagList, CloudTag newTag) {
        if (newTag == null) throw new IllegalArgumentException("Tag cannot be null");
        if (!newTag.isValid()) throw new IllegalArgumentException("Invalid tag");
        if (isExistingTag(tagList, newTag)) throw new IllegalArgumentException("Tag already exists");
        tagList.add(newTag);
        return newTag;
    }

    private boolean isExistingTag(List<CloudTag> tagList, CloudTag targetTag) {
        return tagList.stream()
                .filter(tag -> tag.getName().equals(targetTag.getName()))
                .findAny()
                .isPresent();
    }
}
