package address.events;

import java.io.File;

/**
 * Indicates that the desired mirror location has changed
 */
public class MirrorLocationChangedEvent {

    /** The new file */
    public File mirror;

    public MirrorLocationChangedEvent(File file){
        this.mirror = file;
    }

    @Override
    public String toString(){
        return mirror == null ? "Mirror file location cleared." : "New mirror file location is: " + mirror;
    }
}
