package address.events;

import address.model.datatypes.Tag;
import address.model.datatypes.Person;

import java.util.List;

/** Indicates data in the model has changed*/
public class LocalModelChangedEvent extends BaseEvent {

    public List<Person> personData;
    public List<Tag> tagData;

    public LocalModelChangedEvent(List<Person> personData, List<Tag> tagData){
        this.personData = personData;
        this.tagData = tagData;
    }

    @Override
    public String toString(){
        return "number of persons " + personData.size() + ", number of tags " + tagData.size();
    }
}
