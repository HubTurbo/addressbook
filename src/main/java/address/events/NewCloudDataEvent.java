package address.events;

import address.model.datatypes.AddressBook;

/** Indicates some new data is available from the cloud*/
public class NewCloudDataEvent extends BaseEvent {

    public AddressBook data;

    public NewCloudDataEvent(AddressBook data){
        this.data = data;
    }

    @Override
    public String toString(){
        return "number of persons " + data.getPersons().size()  + ", number of tags " + data.getTags().size();
    }
}
