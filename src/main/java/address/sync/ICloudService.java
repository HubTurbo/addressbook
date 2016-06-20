package address.sync;

import address.model.datatypes.tag.Tag;
import address.model.datatypes.person.Person;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public interface ICloudService {
    // Consumes API quota
    ExtractedCloudResponse<List<Person>> getPersons(String addressBookName) throws IOException;
    ExtractedCloudResponse<List<Tag>> getTags(String addressBookName, String previousETag) throws IOException;

    ExtractedCloudResponse<Person> createPerson(String addressBookName, Person person) throws IOException;
    ExtractedCloudResponse<Person> updatePerson(String addressBookName, int personId, Person updatedPerson)
            throws IOException;
    ExtractedCloudResponse<Void> deletePerson(String addressBookName, int personId) throws IOException;

    ExtractedCloudResponse<Tag> createTag(String addressBookName, Tag tag) throws IOException;
    ExtractedCloudResponse<Tag> editTag(String addressBookName, String oldTagName, Tag newTag) throws IOException;
    ExtractedCloudResponse<Void> deleteTag(String addressBookName, String tagName) throws IOException;

    ExtractedCloudResponse<Void> createAddressBook(String addressBookName) throws IOException;

    ExtractedCloudResponse<List<Person>> getUpdatedPersonsSince(String addressBookName, LocalDateTime time)
            throws IOException;

    // Does not consume API
    ExtractedCloudResponse<HashMap<String, String>> getLimitStatus() throws IOException;

}