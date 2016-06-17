package address.sync;

import address.model.datatypes.person.Person;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * This class is meant to abstract away the details for making requests to the remote
 * Manages RemoteService to obtain the make the appropriate requests
 */
public class RemoteManager {
    RemoteService remoteService;

    String lastPersonsETag;
    LocalDateTime personsLastUpdatedAt;

    public RemoteManager() {
        remoteService = new RemoteService(false);
    }

    public Optional<List<Person>> getUpdatedPersons(String addressBookName) throws IOException {
        // TODO use last updated
        ExtractedRemoteResponse<List<Person>> response = remoteService.getUpdatedPersonsSince(addressBookName, LocalDateTime.now());
        return response.getData();
    }

    private boolean hasValidResponseCode(ExtractedRemoteResponse<List<Person>> response) {
        return response.getResponseCode() <= 300;
    }
}
