package address.sync;

import address.model.datatypes.Tag;
import address.model.datatypes.Person;

import java.io.IOException;
import java.util.List;

public interface ICloudService {
    // Consumes API quota
    ExtractedCloudResponse<List<Person>> getPersons(String addressBookName) throws IOException;
    ExtractedCloudResponse<List<Tag>> getTags(String addressBookName) throws IOException;

    ExtractedCloudResponse<Person> createPerson(String addressBookName, Person person) throws IOException;
    ExtractedCloudResponse<Person> updatePerson(String addressBookName, String oldFirstName, String oldLastName, Person updatedPerson) throws IOException;
    ExtractedCloudResponse<Void> deletePerson(String addressBookName, String firstName, String lastName) throws IOException;

    ExtractedCloudResponse<Tag> createTag(String addressBookName, Tag tag) throws IOException;
    ExtractedCloudResponse<Tag> editTag(String addressBookName, String oldTagName, Tag newTag) throws IOException;
    ExtractedCloudResponse<Void> deleteTag(String addressBookName, String tagName) throws IOException;

    // May consume API
    // Implementation to be figured out to confirm API usage
    ExtractedCloudResponse<List<Person>> getUpdatedPersons(String addressBookName);
    ExtractedCloudResponse<List<Tag>> getUpdatedTags(String addressBookName);

    // Does not consume API
    ExtractedCloudResponse<RateLimitStatus> getLimitStatus() throws IOException;
}
