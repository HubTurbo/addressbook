package address.events;

import address.model.Person;

import java.util.List;

/** Indicates person data in the model has changed*/
public class LocalModelChangedEvent {

    public List<Person> personData;

    public LocalModelChangedEvent(List<Person> personData){
        this.personData = personData;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + " : number of persons " + personData.size();
    }
}
