package address.events;

import address.model.datatypes.Person;
import address.model.datatypes.Tag;

import java.io.File;
import java.util.List;

/**
 * Indicates a request for saving data has been raised
 */
public class SaveRequestEvent extends BaseEvent {

    /** The file to which the data should be saved */
    public File file;

    /** The data to be saved*/
    public List<Person> personData;
    public List<Tag> tagData;

    public SaveRequestEvent(File file, List<Person> personData, List<Tag> tagData){
        this.file = file;
        this.personData = personData;
        this.tagData = tagData;
    }

    @Override
    public String toString(){
        return "" + file;
    }
}
