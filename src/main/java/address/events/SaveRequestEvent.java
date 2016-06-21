package address.events;

import address.model.datatypes.ReadOnlyAddressBook;
import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;

import java.io.File;
import java.util.List;

/**
 * Indicates a request for saving data has been raised
 */
public class SaveRequestEvent extends BaseEvent {

    /** The file to which the data should be saved */
    public final File file;

    public final ReadOnlyAddressBook data;

    public SaveRequestEvent(File file, ReadOnlyAddressBook data){
        this.file = file;
        this.data = data;
    }

    @Override
    public String toString(){
        return "" + file;
    }
}
