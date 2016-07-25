package address.events.sync;

import address.events.BaseEvent;
import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;

import java.util.List;
import java.util.Optional;

/**
 * An event triggered when Syncing (down) is completed.
 *
 * Contains the data obtained from the sync request.
 */
public class SyncCompletedEvent extends BaseEvent {
    List<Person> updatedPersons;
    Optional<List<Tag>> latestTags;

    public SyncCompletedEvent(List<Person> updatedPersons, Optional<List<Tag>> latestTags) {
        this.updatedPersons = updatedPersons;
        this.latestTags = latestTags;
    }

    public Optional<List<Tag>> getLatestTags() {
        return latestTags;
    }

    public List<Person> getUpdatedPersons() {
        return updatedPersons;
    }

    @Override
    public String toString() {
        String stringToReturn = updatedPersons.size() + " updatedPersons";
        stringToReturn += latestTags.isPresent() ? " and " + latestTags.get().size() + " latest tags"
                                                 : " and no updates to tags";
        return stringToReturn;
    }
}
