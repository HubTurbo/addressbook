package address.sync;

import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * This class is meant to abstract away the details for making requests to the remote
 * Manages RemoteService to obtain the make the appropriate requests
 */
public class RemoteManager {
    RemoteService remoteService;

    HashMap<String, HashMap<Integer, LastUpdate>> updateInformation;
    LocalDateTime personLastUpdatedAt;

    public RemoteManager() {
        remoteService = new RemoteService(false);
    }

    public Optional<List<Person>> getUpdatedPersons(String addressBookName) throws IOException {
        ExtractedRemoteResponse<List<Person>> response;
        if (personLastUpdatedAt == null) {
            response = remoteService.getPersons(addressBookName);
        } else {
            response = remoteService.getUpdatedPersonsSince(addressBookName, personLastUpdatedAt);
        }
        personLastUpdatedAt = LocalDateTime.now();
        return response.getData();
    }

    /**
     * Returns the full list of tags if there are updates
     *
     * @param addressBookName
     * @return empty optional if there are no updates or if there are other errors
     * @throws IOException
     */
    public Optional<List<Tag>> getTagsIfUpdated(String addressBookName) throws IOException {
        ExtractedRemoteResponse<List<Tag>> response;

        List<Tag> tagList = new ArrayList<>();
        int curPage = 1;
        do {
            Optional<String> lastETag = getLastUpdate(updateInformation, addressBookName, curPage);
            if (lastETag.isPresent()) {
                response = remoteService.getTags(addressBookName, curPage, null);
            } else {
                response = remoteService.getTags(addressBookName, curPage, lastETag.get());
            }
            if (!response.getData().isPresent()) return Optional.empty();
            tagList.addAll(response.getData().get());
            curPage++;
        } while (response.getNextPage() != -1);

        return Optional.of(tagList);
    }

    private Optional<String> getLastUpdate(HashMap<String, HashMap<Integer, LastUpdate>> updateInformation, String addressBookName, Integer pageNo) {
        if (!updateInformation.containsKey(addressBookName)) return Optional.empty();
        HashMap<Integer, LastUpdate> lastUpdateInformation = updateInformation.get(addressBookName);
        if (!lastUpdateInformation.containsKey(pageNo)) return Optional.empty();
        return Optional.of(lastUpdateInformation.get(pageNo).getETag());
    }

    private boolean hasValidResponseCode(ExtractedRemoteResponse<List<Person>> response) {
        return response.getResponseCode() <= 300;
    }
}
