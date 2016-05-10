package address.events;

import address.model.AddressBookWrapper;
import address.model.Person;

import java.util.List;

/** Indicates some new data is available from the mirror*/
public class NewMirrorDataEvent {

    public AddressBookWrapper data;

    public NewMirrorDataEvent(AddressBookWrapper data){
        this.data = data;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + " : number of persons " + data.getPersons().size()
                + ", number of groups " + data.getGroups().size();
    }
}
