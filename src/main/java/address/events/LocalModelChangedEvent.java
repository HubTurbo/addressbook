package address.events;

import address.model.ModelContactGroup;
import address.model.ModelPerson;

import java.util.List;

/** Indicates data in the model has changed*/
public class LocalModelChangedEvent {

    public List<ModelPerson> personData;
    public List<ModelContactGroup> groupData;

    public LocalModelChangedEvent(List<ModelPerson> personData, List<ModelContactGroup> groupData){
        this.personData = personData;
        this.groupData = groupData;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + " : number of persons " + personData.size()
                + ", number of groups " + groupData.size();
    }
}
