package address.sync;

import address.model.ContactGroup;
import address.model.Person;

import java.io.IOException;
import java.util.List;

public interface ICloudService {
    // Consumes API quota
    ExtractedCloudResponse<List<Person>> getPersons(String addressBookName) throws IOException;
    ExtractedCloudResponse<List<ContactGroup>> getGroups(String addressBookName) throws IOException;

    ExtractedCloudResponse<Person> createPerson(String addressBookName, Person person) throws IOException;
    ExtractedCloudResponse<Person> updatePerson(String addressBookName, String oldFirstName, String oldLastName, Person updatedPerson) throws IOException;
    ExtractedCloudResponse<Boolean> deletePerson(String addressBookName, int personId);

    ExtractedCloudResponse<ContactGroup> createGroup(String addressBookName, ContactGroup group);
    ExtractedCloudResponse<ContactGroup> editGroup(String addressBookName, String oldGroupName, ContactGroup newGroup);
    ExtractedCloudResponse<Boolean> deleteGroup(String addressBookName, String groupName);

    // May consume API
    // Implementation to be figured out to confirm API usage
    ExtractedCloudResponse<List<Person>> getUpdatedPersons(String addressBookName);
    ExtractedCloudResponse<List<ContactGroup>> getUpdatedGroups(String addressBookName);

    // Does not consume API
    RateLimitStatus getLimitStatus();
}
