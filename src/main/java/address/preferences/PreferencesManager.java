package address.preferences;

import address.events.EventManager;
import address.events.FileNameChangedEvent;

import java.io.File;

/**
 * Manages saving/retrieving of preferences in the registry.
  */
public class PreferencesManager {

    public static final String REGISTER_FILE_PATH = "address-book-filePath1";
    private static PreferencesManager instance;

    public static PreferencesManager getInstance(){
        if (instance == null){
            instance = new PreferencesManager();
        }

        return instance;
    }
    /**
     * Returns the person file preference, i.e. the file that was last opened.
     * The preference is read from the OS specific registry. If no such
     * preference can be found, null is returned.
     *
     */
    public File getPersonFilePath() {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(PreferencesManager.class);
        String filePath = prefs.get(REGISTER_FILE_PATH, null);
        if (filePath != null) {
            return new File(filePath);
        } else {
            System.out.println("file path not found ");
            return null;
        }
    }

    /**
     * Sets the file path of the currently loaded file. The path is persisted in
     * the OS specific registry.
     *
     * @param file the file or null to remove the path
     */
    public void setPersonFilePath(File file) {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(PreferencesManager.class);
        if (file != null) {
            prefs.put(REGISTER_FILE_PATH, file.getPath());
        } else {
            prefs.remove(REGISTER_FILE_PATH);
        }

        EventManager.getInstance().post(new FileNameChangedEvent(file));
    }
}
