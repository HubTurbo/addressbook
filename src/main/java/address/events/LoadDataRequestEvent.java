package address.events;

import address.model.Person;
import javafx.collections.ObservableList;

import java.io.File;

/**
 * Indicates a request to load data from the file
 */
public class LoadDataRequestEvent {

    /** The file from which the data to be loaded */
    public File file;

    /** The container for loading the data */
    public ObservableList<Person> personData;

    public LoadDataRequestEvent(File file, ObservableList<Person> personData){
        this.file = file;
        this.personData = personData;
    }
}
