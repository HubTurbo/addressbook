package address.events;

import address.model.ContactGroup;
import address.model.Person;

import java.io.File;
import java.util.List;

/**
 * Indicates a request for deleting a person
 */
public class DeleteRequestEvent {


    @Override
    public String toString(){
        return this.getClass().getSimpleName();
    }
}
