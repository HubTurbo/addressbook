package address.preferences;

import address.events.EventManager;
import address.events.FileNameChangedEvent;

import java.io.File;

/**
 * Manages saving/retrieving of preferences in the registry.
  */
public class PreferencesManager {

    public static final String REGISTER_FILE_PATH = "address-book-filePath1";
    public static final String DEFAULT_PERSON_FILE_PATH_PREFIX = "address_book";
    public static final String FILE_FORMAT_POSTFIX = ".xml";

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
     * preference can be found, the default  is returned.
     *
     */
    public File getPersonFile() {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(PreferencesManager.class);
        String filePath = prefs.get(PreferencesManager.appTitle + "/" + REGISTER_FILE_PATH, null);
        if (filePath == null) {
            System.out.println("file path not found, using default file path");
            filePath = DEFAULT_PERSON_FILE_PATH_PREFIX + FILE_FORMAT_POSTFIX;
        }
        return new File(filePath);
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
