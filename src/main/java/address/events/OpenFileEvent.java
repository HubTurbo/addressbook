package address.events;

import address.model.Person;
import javafx.collections.ObservableList;

import java.io.File;

public class OpenFileEvent {

    public File file;
    public ObservableList<Person> personData;

    public OpenFileEvent(File file, ObservableList<Person> personData){
        this.file = file;
        this.personData = personData;
    }
}
