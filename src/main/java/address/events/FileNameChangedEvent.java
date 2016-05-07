package address.events;

import java.io.File;

public class FileNameChangedEvent {

    public File file;

    public FileNameChangedEvent(File file){
        this.file = file;
    }
}
