package address.sync.cloud;

import address.sync.cloud.model.CloudPerson;
import address.sync.cloud.model.CloudTag;

public interface IRemote {
    RemoteResponse createPerson(String addressBookName, CloudPerson newPerson, String previousETag);
    RemoteResponse getPersons(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag);
    RemoteResponse getUpdatedPersons(String addressBookName, String timeString, int pageNumber, int resourcesPerPage, String previousETag);
    RemoteResponse getTags(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag);
    RemoteResponse getRateLimitStatus(String previousETag);
    RemoteResponse updatePerson(String addressBookName, int personId, CloudPerson updatedPerson, String previousETag);
    RemoteResponse deletePerson(String addressBookName, int personId);
    RemoteResponse createTag(String addressBookName, CloudTag newTag, String previousETag);
    RemoteResponse editTag(String addressBookName, String oldTagName, CloudTag updatedTag, String previousETag);
    RemoteResponse deleteTag(String addressBookName, String tagName);
}
