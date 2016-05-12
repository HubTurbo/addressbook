package address.events;

import address.model.ContactGroup;
import address.model.Person;
import javafx.collections.ObservableList;

import java.io.File;

/**
 * Indicates a request to load data from the file
 */
public class LoadDataRequestEvent {

    /** The file from which the data to be loaded */
    public File file;

    public LoadDataRequestEvent(File file) {
        this.file = file;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + " : from " + file;
    }
}
