package address.events;

import address.model.datatypes.tag.Tag;
import address.model.datatypes.person.Person;

import java.util.List;

/** Indicates person data in the model was synced with data on the cloud */
public class LocalModelSyncedFromCloudEvent extends BaseEvent {

    public List<Person> personData;

    public List<Tag> tagData;

    public LocalModelSyncedFromCloudEvent(List<Person> personData, List<Tag> tagData) {
        this.personData = personData;
        this.tagData = tagData;
    }

    @Override
    public String toString(){
        return "number of persons: " + personData.size() + ", number of tag" +
                "s: " + tagData.size();
    }
}
