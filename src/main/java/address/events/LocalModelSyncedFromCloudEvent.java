package address.events;

import address.model.ContactGroup;
import address.model.ModelContactGroup;
import address.model.ModelPerson;
import address.model.Person;

import java.util.List;

/** Indicates person data in the model was synced with data on the cloud */
public class LocalModelSyncedFromCloudEvent extends BaseEvent {

    public List<ModelPerson> personData;

    public List<ModelContactGroup> groupData;

    public LocalModelSyncedFromCloudEvent(List<ModelPerson> personData, List<ModelContactGroup> groupData) {
        this.personData = personData;
        this.groupData = groupData;
    }

    @Override
    public String toString(){
        return "number of persons: " + personData.size() + ", number of groups: " + groupData.size();
    }
}
