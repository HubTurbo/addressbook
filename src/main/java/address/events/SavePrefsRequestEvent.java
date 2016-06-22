package address.events;

import address.model.UserPrefs;

import java.io.File;

/**
 * Indicates a request for saving preferences has been raised
 */
public class SavePrefsRequestEvent extends BaseEvent {
    /** The file to which the data should be saved */
    public final File file;

    public final UserPrefs prefs;

    public SavePrefsRequestEvent(File file, UserPrefs prefs) {
        this.file = file;
        this.prefs = prefs;
    }

    @Override
    public String toString(){
        return "" + file.getAbsolutePath();
    }
}
