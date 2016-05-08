package address.events;

import address.model.Person;

import java.util.List;

/** Indicates some new data is available from the mirror*/
public class NewMirrorDataEvent {

    public List<Person> personData;

    public NewMirrorDataEvent(List<Person> personData){
        this.personData = personData;
    }
}
