package address.events;

import java.io.File;

/**
 * Indicates an exception during a file saving
 */
public class FileSavingExceptionEvent {

    public Exception exception;
    public File file;

    public FileSavingExceptionEvent(Exception exception, File file){
        this.exception = exception;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + " : " + exception.toString();
    }
}
