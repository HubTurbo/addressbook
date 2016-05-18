package address.events;

import java.io.File;

/**
 * Indicates the name of the data file has been changed
 */
public class SaveFileChangedEvent extends BaseEvent {

    /** The new file */
    public File file;

    public SaveFileChangedEvent(File file){
        this.file = file;
    }

    @Override
    public String toString(){
        return file == null ? "File name cleared" : "New file name is " + file;
    }
}
