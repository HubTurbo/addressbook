package address.events;

import address.model.ContactGroup;
import address.model.Person;

import java.util.List;

/** Indicates person data in the model has changed*/
public class LocalModelChangedEvent {

    public List<Person> personData;
    public List<ContactGroup> groupData;

    public LocalModelChangedEvent(List<Person> personData, List<ContactGroup> groupData){
        this.personData = personData;
        this.groupData = groupData;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + " : number of persons " + personData.size();
    }
}
