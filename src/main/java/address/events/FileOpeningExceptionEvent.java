package address.events;

import address.model.Person;
import javafx.collections.ObservableList;

import java.io.File;

public class FileOpeningExceptionEvent {

    public Exception exception;
    public File file;

    public FileOpeningExceptionEvent(Exception exception, File file){
        this.exception = exception;
    }
}
