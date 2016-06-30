package address.events;

import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;

import java.util.List;

/**
 * An event triggered when Syncing is completed.
 */
public class SyncCompletedEvent extends BaseEvent {
    List<Person> updatedPersons;
    List<Tag> latestTags;

    public SyncCompletedEvent(List<Person> updatedPersons, List<Tag> latestTags) {
        this.updatedPersons = updatedPersons;
        this.latestTags = latestTags;
    }

    public List<Tag> getLatestTags() {
        return latestTags;
    }

    public List<Person> getUpdatedPersons() {
        return updatedPersons;
    }

    @Override
    public String toString() {
        return "Synchronization is completed: " + updatedPersons.size() + " updatedPersons and " + latestTags.size() + " latest tags.";
    }
}
