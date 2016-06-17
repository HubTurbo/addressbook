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

    HashMap<String, LastUpdate> updateInformation;
    LocalDateTime personLastUpdatedAt;

    public RemoteManager() {
        updateInformation = new HashMap<>();
        remoteService = new RemoteService(false);
    }

    public Optional<List<Person>> getUpdatedPersons(String addressBookName) throws IOException {
        ExtractedRemoteResponse<List<Person>> response;

        int curPage = 1;
        do {
            if (personLastUpdatedAt == null) {
                response = remoteService.getPersons(addressBookName, curPage);
            } else {
                response = remoteService.getUpdatedPersonsSince(addressBookName, curPage, personLastUpdatedAt, null);
            }
        } while (response.getNextPage() != -1); // may have problems if RESOURCES_PER_PAGE issues have been updated at the same second
                                                // of the update request, since the second page will never be requested, and first page
                                                // will always remain the same
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
        LastUpdate lastUpdateInfo = new LastUpdate();
        int curPage = 1;
        int prevPageCount = getLastUpdatedPageCount(updateInformation, addressBookName);
        do {
            Optional<String> lastETag = getLastUpdate(updateInformation, addressBookName, curPage);
            if (lastETag.isPresent()) {
                response = remoteService.getTags(addressBookName, curPage, lastETag.get());
            } else {
                response = remoteService.getTags(addressBookName, curPage, null);
            }
            if (!response.getData().isPresent()) return Optional.empty();
            lastUpdateInfo.setETag(curPage, response.getETag());
            tagList.addAll(response.getData().get());
            curPage++;
        } while (response.getNextPage() != -1 || curPage < prevPageCount);// does not handle the case moving from a fully-filled last page -> a new page with new tags
        lastUpdateInfo.setLastUpdatedAt(LocalDateTime.now());
        updateInformation.put(addressBookName, lastUpdateInfo);
        
        return Optional.of(tagList);
    }

    private int getLastUpdatedPageCount(HashMap<String, LastUpdate> updateInformation, String addressBookName) {
        if (!updateInformation.containsKey(addressBookName)) return 0;
        return updateInformation.get(addressBookName).getETagCount();
    }

    private Optional<String> getLastUpdate(HashMap<String, LastUpdate> updateInformation, String addressBookName, Integer pageNo) {
        if (!updateInformation.containsKey(addressBookName)) return Optional.empty();
        LastUpdate lastUpdateInformation = updateInformation.get(addressBookName);
        return lastUpdateInformation.getETag(pageNo);
    }
}
