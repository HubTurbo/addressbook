package address.events;

import java.io.File;

/**
 * Indicates the name of the data file has been changed
 */
public class FileNameChangedEvent {

    /** The new file */
    public File file;

    public FileNameChangedEvent(File file){
        this.file = file;
    }

    @Override
    public String toString(){
        final String className = this.getClass().getSimpleName();
        if (file == null) {
            return className + " : File name cleared";
        } else {
            return className + " : New file name is " + file;
        }
    }
}
