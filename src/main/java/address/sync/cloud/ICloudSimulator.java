package address.sync.cloud;

import address.sync.model.RemotePerson;
import address.sync.model.RemoteTag;

public interface ICloudSimulator {
    CloudResponse createPerson(String addressBookName, RemotePerson newPerson, String previousETag);
    CloudResponse getPersons(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag);
    CloudResponse getUpdatedPersons(String addressBookName, String timeString, int pageNumber, int resourcesPerPage, String previousETag);
    CloudResponse getTags(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag);
    CloudResponse getRateLimitStatus(String previousETag);
    CloudResponse updatePerson(String addressBookName, int personId, RemotePerson updatedPerson, String previousETag);
    CloudResponse deletePerson(String addressBookName, int personId);
    CloudResponse createTag(String addressBookName, RemoteTag newTag, String previousETag);
    CloudResponse editTag(String addressBookName, String oldTagName, RemoteTag updatedTag, String previousETag);
    CloudResponse deleteTag(String addressBookName, String tagName);
    CloudResponse createAddressBook(String addressBookName);
}
