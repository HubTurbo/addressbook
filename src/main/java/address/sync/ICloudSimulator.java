package address.sync;

import address.model.ContactGroup;
import address.model.Person;

import java.util.List;
import java.util.Optional;

public interface ICloudSimulator {
    // Consumes API quota
    Optional<CloudResponse<List<Person>>> getPersons(String addressBookName);
    Optional<CloudResponse<List<ContactGroup>>> getGroups(String addressBookName);

    Optional<CloudResponse<Person>> createPerson(String addressBookName, Person person);
    Optional<CloudResponse<Person>> updatePerson(String addressBookName, String oldFirstName, String oldLastName, Person updatedPerson);
    Optional<Boolean> deletePerson(String addressBookName, int personId);

    Optional<CloudResponse> createGroup(String addressBookName, ContactGroup group);
    Optional<CloudResponse> editGroup(String addressBookName, String oldGroupName, ContactGroup newGroup);
    Optional<Boolean> deleteGroup(String addressBookName, String groupName);

    // May consume API
    // Implementation to be figured out to confirm API usage
    Optional<CloudResponse> getUpdatedPersons(String addressBookName);
    Optional<CloudResponse> getUpdatedGroups(String addressBookName);

    // Does not consume API
    int getQuotaLimit();
    int getQuotaRemaining();
    long getQuotaReset();
}
