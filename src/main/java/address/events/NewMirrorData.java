package address.events;

import address.model.Person;

import java.util.List;

/** Indicates some new data is available from the mirror*/
public class NewMirrorData {

    public List<Person> personData;

    public NewMirrorData(List<Person> personData){
        this.personData = personData;
    }
}
