package address.events;

import java.io.File;

/**
 * Indicates the desired save location has changed
 */
public class SaveLocationChangedEvent extends BaseEvent {

    /** The new file */
    public File save;

    public SaveLocationChangedEvent(File file){
        this.save = file;
    }

    @Override
    public String toString(){
        return save == null ? "Save file location cleared." : "New save file location is: " + save;
    }
}
