package address.events;

import address.model.datatypes.ContactGroup;
import address.model.datatypes.Person;

import java.util.List;

/** Indicates person data in the model was synced with data on the cloud */
public class LocalModelSyncedFromCloudEvent extends BaseEvent {

    public List<Person> personData;

    public List<ContactGroup> groupData;

    public LocalModelSyncedFromCloudEvent(List<Person> personData, List<ContactGroup> groupData) {
        this.personData = personData;
        this.groupData = groupData;
    }

    @Override
    public String toString(){
        return "number of persons: " + personData.size() + ", number of groups: " + groupData.size();
    }
}
