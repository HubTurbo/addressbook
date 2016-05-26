package address.sync;

import address.model.ContactGroup;
import address.model.Person;

import java.util.List;

public interface ICloudSimulator {
    // Consumes API quota
    CloudResponse<List<Person>> getPersons(String addressBookName);
    CloudResponse<List<ContactGroup>> getGroups(String addressBookName);

    CloudResponse<Person> createPerson(String addressBookName, Person person);
    CloudResponse<Person> updatePerson(String addressBookName, String oldFirstName, String oldLastName, Person updatedPerson);
    CloudResponse<Boolean> deletePerson(String addressBookName, int personId);

    CloudResponse<ContactGroup> createGroup(String addressBookName, ContactGroup group);
    CloudResponse<ContactGroup> editGroup(String addressBookName, String oldGroupName, ContactGroup newGroup);
    CloudResponse<Boolean> deleteGroup(String addressBookName, String groupName);

    // May consume API
    // Implementation to be figured out to confirm API usage
    CloudResponse<List<Person>> getUpdatedPersons(String addressBookName);
    CloudResponse<List<ContactGroup>> getUpdatedGroups(String addressBookName);

    // Does not consume API
    CloudResponse<RateLimitStatus> getLimitStatus();
}
