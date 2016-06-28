package address.sync;

import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;
import address.util.AppLogger;
import address.util.Config;
import address.util.LoggerManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * This class is meant to abstract away the details for making requests to the remote
 * Manages RemoteService to obtain the make the appropriate requests, and keeps track of
 * update information to reduce usage of API quota given by the remote
 */
public class RemoteManager {
    private static final AppLogger logger = LoggerManager.getLogger(RemoteManager.class);

    RemoteService remoteService;

    HashMap<String, LastUpdate<Tag>> updateInformation;
    LocalDateTime personLastUpdatedAt;

    public RemoteManager(Config config) {
        updateInformation = new HashMap<>();
        remoteService = new RemoteService(config);
    }

    public RemoteManager(RemoteService remoteService) {
        updateInformation = new HashMap<>();
        this.remoteService = remoteService;
    }

    /**
     * Attempts to get the list of updated persons since the last update, if it exists
     * Else simply attempts to get the full list of persons
     *
     * @param addressBookName
     * @return
     * @throws IOException
     */
    public Optional<List<Person>> getUpdatedPersons(String addressBookName) throws IOException {
        ExtractedRemoteResponse<List<Person>> response;

        List<Person> personList = new ArrayList<>();
        int curPage = 1;
        logger.info("Getting updated persons from remote.");
        do {
            if (personLastUpdatedAt == null) {
                logger.debug("No previous update found, retrieving page {}", curPage);
                response = remoteService.getPersons(addressBookName, curPage);
            } else {
                logger.debug("Last updated time for page {} found: {}", curPage, personLastUpdatedAt);
                response = remoteService.getUpdatedPersonsSince(addressBookName, curPage, personLastUpdatedAt, null);
            }
            if (!response.getData().isPresent()) return Optional.empty();
            personList.addAll(response.getData().get());
            curPage++;
        } while (response.getNextPage() != 0); // may have problems if RESOURCES_PER_PAGE issues have been updated at the same second
                                                // of the update request, since the second page will never be requested, and first page
                                                // will always remain the same
        logger.info("{} updated persons.", personList.size());
        personLastUpdatedAt = LocalDateTime.now();
        return Optional.of(personList);
    }

    /**
     * Returns the full list of updated tags
     *
     * @param addressBookName
     * @return empty optional if there are no updates or if there are other errors
     * @throws IOException
     */
    public Optional<List<Tag>> getUpdatedTagList(String addressBookName) throws IOException {
        ExtractedRemoteResponse<List<Tag>> response;

        List<Tag> tagList = new ArrayList<>();
        LastUpdate<Tag> lastUpdateInfo = new LastUpdate<>();
        int curPage = 1;
        int prevPageCount = getLastUpdatedPageCount(updateInformation, addressBookName);
        logger.info("Getting tags list from remote.");
        do {
            Optional<String> lastETag = getLastUpdate(updateInformation, addressBookName, curPage);
            if (lastETag.isPresent()) {
                logger.debug("Last eTag for page {} found: {}", curPage, lastETag.get());
                response = remoteService.getTags(addressBookName, curPage, lastETag.get());
            } else {
                logger.debug("No previous eTag for page {} found.", curPage);
                response = remoteService.getTags(addressBookName, curPage, null);
            }
            
            if (response.getData().isPresent()) {
                logger.debug("New tags for page {} found: {}", curPage, response.getData().get());
                lastUpdateInfo.setUpdate(curPage, response.getETag(), response.getData().get());
                tagList.addAll(response.getData().get());
            } else {
                Optional<List<Tag>> previousUpdateList = lastUpdateInfo.getResourceList(curPage);
                if (!previousUpdateList.isPresent()) return Optional.empty();
                logger.debug("No new tags for page {}, using last known: {}", curPage, previousUpdateList.get());
                tagList.addAll(previousUpdateList.get());
            }
            curPage++;
        } while (response.getNextPage() != 0 || curPage < prevPageCount);// does not handle the case moving from a fully-filled last page -> a new page with new tags
        lastUpdateInfo.setLastUpdatedAt(LocalDateTime.now());
        updateInformation.put(addressBookName, lastUpdateInfo);
        
        return Optional.of(tagList);
    }

    /**
     * Attempts to create a person on the cloud
     *
     * @param addressBookName
     * @param person
     * @return Empty optional if fail to create person or if there is a network error
     * @throws IOException
     */
    public Optional<Person> createPerson(String addressBookName, Person person) throws IOException {
        ExtractedRemoteResponse<Person> response = remoteService.createPerson(addressBookName, person);
        return response.getData();
    }

    /**
     * Attempts to create a tag on the cloud
     *
     * @param addressBookName
     * @param tag
     * @return Empty optional if fail to create tag or if there is a network error
     * @throws IOException
     */
    public Optional<Tag> createTag(String addressBookName, Tag tag) throws IOException {
        ExtractedRemoteResponse<Tag> response = remoteService.createTag(addressBookName, tag);
        return response.getData();
    }

    /**
     * Attempts to update a person on the cloud
     * @param addressBookName
     * @param personId id of the person to be updated
     * @param updatedPerson updated person
     * @return Empty optional if fail to update person or if there is a network error
     * @throws IOException
     */
    public Optional<Person> updatePerson(String addressBookName, int personId, Person updatedPerson) throws IOException {
        ExtractedRemoteResponse<Person> response = remoteService.updatePerson(addressBookName, personId, updatedPerson);
        return response.getData();
    }

    /**
     * Attempts to edit a tag on the cloud
     * @param addressBookName
     * @param tagName name of the tag
     * @param editedTag edited tag
     * @return Empty optional if fail to edit tag or if there is a network error
     * @throws IOException
     */
    public Optional<Tag> editTag(String addressBookName, String tagName, Tag editedTag) throws IOException {
        ExtractedRemoteResponse<Tag> response = remoteService.editTag(addressBookName, tagName, editedTag);
        return response.getData();
    }

    private <T> int getLastUpdatedPageCount(HashMap<String, LastUpdate<T>> updateInformation, String addressBookName) {
        if (!updateInformation.containsKey(addressBookName)) return 0;
        return updateInformation.get(addressBookName).getETagCount();
    }

    private <T> Optional<String> getLastUpdate(HashMap<String, LastUpdate<T>> updateInformation, String addressBookName,
                                               Integer pageNo) {
        if (!updateInformation.containsKey(addressBookName)) return Optional.empty();
        LastUpdate<T> lastUpdateInformation = updateInformation.get(addressBookName);
        return lastUpdateInformation.getETag(pageNo);
    }
}
