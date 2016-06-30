package address.events;

import java.io.File;

/**
 * Indicates the desired save location has changed
 */
public class SaveLocationChangedEvent extends BaseEvent {

    /** The new file */
    public final File saveFile;

    public SaveLocationChangedEvent(File file){
        this.saveFile = file;
    }

    public SaveLocationChangedEvent(String filePath){
        if (filePath == null) {
            this.saveFile = null;
        } else {
            this.saveFile = new File(filePath);
        }
    }

    @Override
    public String toString(){
        return saveFile == null ? "Save file location cleared." : "New save file location is: " + saveFile;
    }
}
