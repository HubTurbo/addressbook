package address.sync;

import address.sync.model.CloudTag;
import address.sync.model.CloudPerson;

public interface ICloudSimulator {
    RawCloudResponse createPerson(String addressBookName, CloudPerson newPerson, String previousETag);
    RawCloudResponse getPersons(String addressBookName, int resourcesPerPage, String previousETag);
    RawCloudResponse getUpdatedPersons(String addressBookName, String timeString, int resourcesPerPage, String previousETag);
    RawCloudResponse getTags(String addressBookName, int resourcesPerPage, String previousETag);
    RawCloudResponse getRateLimitStatus(String previousETag);
    RawCloudResponse updatePerson(String addressBookName, String oldFirstName, String oldLastName, CloudPerson updatedPerson, String previousETag);
    RawCloudResponse deletePerson(String addressBookName, String firstName, String lastName);
    RawCloudResponse createTag(String addressBookName, CloudTag newTag, String previousETag);
    RawCloudResponse editTag(String addressBookName, String oldTagName, CloudTag updatedTag, String previousETag);
    RawCloudResponse deleteTag(String addressBookName, String tagName);
    RawCloudResponse createAddressBook(String addressBookName);
}
