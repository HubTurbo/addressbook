package address.events;

import address.model.AddressBook;

/** Indicates some new data is available from the mirror*/
public class NewMirrorDataEvent extends BaseEvent {

    public AddressBook data;

    public NewMirrorDataEvent(AddressBook data){
        this.data = data;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + " : number of persons " + data.getPersons().size()
                + ", number of groups " + data.getGroups().size();
    }
}
