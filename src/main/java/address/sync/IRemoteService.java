package address.sync;

import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.tag.Tag;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public interface IRemoteService {
    // Consumes API quota
    ExtractedRemoteResponse<List<Person>> getPersons(String addressBookName, int pageNumber) throws IOException;

    ExtractedRemoteResponse<List<Tag>> getTags(String addressBookName, int pageNumber, String previousETag)
            throws IOException;

    ExtractedRemoteResponse<Person> createPerson(String addressBookName, ReadOnlyPerson person) throws IOException;
    ExtractedRemoteResponse<Person> updatePerson(String addressBookName, int personId, ReadOnlyPerson updatedPerson)
            throws IOException;
    ExtractedRemoteResponse<Void> deletePerson(String addressBookName, int personId) throws IOException;

    ExtractedRemoteResponse<Tag> createTag(String addressBookName, Tag tag) throws IOException;
    ExtractedRemoteResponse<Tag> editTag(String addressBookName, String oldTagName, Tag newTag) throws IOException;
    ExtractedRemoteResponse<Void> deleteTag(String addressBookName, String tagName) throws IOException;

    ExtractedRemoteResponse<Void> createAddressBook(String addressBookName) throws IOException;

    ExtractedRemoteResponse<List<Person>> getUpdatedPersonsSince(String addressBookName, int curPageNumber,
                                                                 LocalDateTime time, String previousETag)
            throws IOException;

    // Does not consume API
    ExtractedRemoteResponse<HashMap<String, String>> getLimitStatus() throws IOException;

}
