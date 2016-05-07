package address.events;

import address.model.Person;

import java.util.List;

public class NewDataEvent {

    public List<Person> personData;

    public NewDataEvent(List<Person> personData){
        this.personData = personData;
    }
}
