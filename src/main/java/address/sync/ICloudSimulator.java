package address.sync;

import address.sync.model.CloudGroup;
import address.sync.model.CloudPerson;

public interface ICloudSimulator {
    RawCloudResponse createPerson(String addressBookName, CloudPerson newPerson);
    RawCloudResponse getPersons(String addressBookName, int resourcesPerPage);
    RawCloudResponse getGroups(String addressBookName, int resourcesPerPage);
    RawCloudResponse getRateLimitStatus();
    RawCloudResponse updatePerson(String addressBookName, String oldFirstName, String oldLastName, CloudPerson updatedPerson);
    RawCloudResponse deletePerson(String addressBookName, String firstName, String lastName);
    RawCloudResponse createGroup(String addressBookName, CloudGroup newGroup);
    RawCloudResponse editGroup(String addressBookName, String oldGroupName, CloudGroup updatedGroup);
    RawCloudResponse deleteGroup(String addressBookName, String groupName);
}
