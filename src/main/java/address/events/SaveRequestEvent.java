package address.events;

import address.model.ContactGroup;
import address.model.Person;

import java.io.File;
import java.util.List;

/**
 * Indicates a request for saving data has been raised
 */
public class SaveRequestEvent {

    /** The file to which the data should be saved */
    public File file;

    /** The data to be saved*/
    public List<Person> personData;
    public List<ContactGroup> groupData;

    public SaveRequestEvent(File file, List<Person> personData, List<ContactGroup> groupData){
        this.file = file;
        this.personData = personData;
        this.groupData = groupData;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + " : " + file;
    }
}
