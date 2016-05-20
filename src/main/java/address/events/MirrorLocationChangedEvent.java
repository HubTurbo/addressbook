package address.events;

import java.io.File;

/**
 * Indicates that the desired mirror location has changed
 */
public class MirrorLocationChangedEvent extends BaseEvent {

    /** The new file */
    public final File mirrorFile;

    public MirrorLocationChangedEvent(File file){
        this.mirrorFile = file;
    }

    @Override
    public String toString(){
        return mirrorFile == null ? "Mirror file location cleared." : "New mirror file location is: " + mirrorFile;
    }
}
