package address.sync.cloud;

import address.sync.cloud.model.CloudPerson;
import address.sync.cloud.model.CloudTag;

public interface ICloudSimulator {
    CloudResponse createPerson(String addressBookName, CloudPerson newPerson, String previousETag);
    CloudResponse getPersons(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag);
    CloudResponse getUpdatedPersons(String addressBookName, String timeString, int pageNumber, int resourcesPerPage, String previousETag);
    CloudResponse getTags(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag);
    CloudResponse getRateLimitStatus(String previousETag);
    CloudResponse updatePerson(String addressBookName, int personId, CloudPerson updatedPerson, String previousETag);
    CloudResponse deletePerson(String addressBookName, int personId);
    CloudResponse createTag(String addressBookName, CloudTag newTag, String previousETag);
    CloudResponse editTag(String addressBookName, String oldTagName, CloudTag updatedTag, String previousETag);
    CloudResponse deleteTag(String addressBookName, String tagName);
    CloudResponse createAddressBook(String addressBookName);
}
