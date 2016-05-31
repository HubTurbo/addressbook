package address.sync;

import address.sync.model.CloudTag;
import address.sync.model.CloudPerson;

public interface ICloudSimulator {
    RawCloudResponse createPerson(String addressBookName, CloudPerson newPerson);
    RawCloudResponse getPersons(String addressBookName, int resourcesPerPage);
    RawCloudResponse getUpdatedPersons(String addressBookName, String timeString, int resourcesPerPage);
    RawCloudResponse getTags(String addressBookName, int resourcesPerPage);
    RawCloudResponse getRateLimitStatus();
    RawCloudResponse updatePerson(String addressBookName, String oldFirstName, String oldLastName, CloudPerson updatedPerson);
    RawCloudResponse deletePerson(String addressBookName, String firstName, String lastName);
    RawCloudResponse createTag(String addressBookName, CloudTag newTag);
    RawCloudResponse editTag(String addressBookName, String oldTagName, CloudTag updatedTag);
    RawCloudResponse deleteTag(String addressBookName, String tagName);
    RawCloudResponse createAddressBook(String addressBookName);
}
