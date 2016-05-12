package address.preferences;

import address.events.EventManager;
import address.events.FileNameChangedEvent;

import java.io.File;

/**
 * Manages saving/retrieving of preferences in the registry.
  */
public class PreferencesManager {

    public static final String REGISTER_FILE_PATH = "address-book-filePath1";
    public static final String DEFAULT_FILE_PATH = "default-address-book.xml";

    private static PreferencesManager instance;

    private static String appTitle = "";

    public static PreferencesManager getInstance(){
        if (instance == null){
            instance = new PreferencesManager();
        }
        return instance;
    }

    public static void setAppTitle(String appTitle) {
        PreferencesManager.appTitle = appTitle;
    }

    /**
     * Returns the person file preference, i.e. the file that was last opened.
     * The preference is read from the OS specific registry. If no such
     * preference can be found, null is returned.
     *
     */
    public File getPersonFile() {
        final java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(PreferencesManager.class);
        final String filePath = prefs.get(PreferencesManager.appTitle + "/" + REGISTER_FILE_PATH, null);
        if (filePath == null) {
            return new File(DEFAULT_FILE_PATH);
        } else {
            return new File(filePath);
        }
    }

    /**
     * Sets the file path of the currently loaded file. The path is persisted in
     * the OS specific registry.
     * @param file the file or null to remove the path
     */
    public void setPersonFilePath(File file) {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(PreferencesManager.class);
        String key = PreferencesManager.appTitle + "/" + REGISTER_FILE_PATH;
        if (file == null) {
            prefs.remove(key);
        } else {
            prefs.put(key, file.getPath());
        }
        EventManager.getInstance().post(new FileNameChangedEvent(file));
    }
}
