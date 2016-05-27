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
    ExtractedCloudResponse<Void> deletePerson(String addressBookName, String firstName, String lastName) throws IOException;

    ExtractedCloudResponse<ContactGroup> createGroup(String addressBookName, ContactGroup group) throws IOException;
    ExtractedCloudResponse<ContactGroup> editGroup(String addressBookName, String oldGroupName, ContactGroup newGroup) throws IOException;
    ExtractedCloudResponse<Void> deleteGroup(String addressBookName, String groupName) throws IOException;

    // May consume API
    // Implementation to be figured out to confirm API usage
    ExtractedCloudResponse<List<Person>> getUpdatedPersons(String addressBookName);
    ExtractedCloudResponse<List<ContactGroup>> getUpdatedGroups(String addressBookName);

    // Does not consume API
    ExtractedCloudResponse<RateLimitStatus> getLimitStatus() throws IOException;
}
