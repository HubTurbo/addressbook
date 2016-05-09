package address.events;

import address.model.Person;

import java.util.List;

/** Indicates person data in the model was synced with data on the cloud */
public class LocalModelSyncedEvent {

    public List<Person> personData;

    public LocalModelSyncedEvent(List<Person> personData){
        this.personData = personData;
    }
}
