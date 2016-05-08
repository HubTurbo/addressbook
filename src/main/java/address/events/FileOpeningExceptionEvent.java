package address.events;

import java.io.File;

/**
 * Indicates an exception during a file opening
 */
public class FileOpeningExceptionEvent {

    public Exception exception;
    public File file;

    public FileOpeningExceptionEvent(Exception exception, File file){
        this.exception = exception;
    }
}
