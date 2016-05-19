package address.prefs;

import address.events.EventManager;
import address.events.SaveFileChangedEvent;

import java.util.prefs.Preferences;
import java.io.File;

/**
 * Manages saving/retrieving of preferences in the registry.
  */
public class PrefsManager {

    public static final String PREF_NODE_NAME_SAVE_FILE = "save-file";
    public static final String PREF_NODE_NAME_MIRROR_FILE = "mirror-file";

    public static final String DEFAULT_TEMP_FILE_PATH = ".$TEMP_ADDRESS_BOOK";

    private static PrefsManager instance;

    public static PrefsManager getInstance(){
        if (instance == null){
            instance = new PrefsManager();
        }
        return instance;
    }

    private PrefsManager() {}

    /**
     * @return the current save file preference or the default temp file if there is no recorded prederence.
     */
    public File getSaveFile() {
        final String filePath = Preferences.userNodeForPackage(PrefsManager.class).get(PREF_NODE_NAME_SAVE_FILE, null);
        if (filePath == null) {
            return new File(DEFAULT_TEMP_FILE_PATH);
        } else {
            return new File(filePath);
        }
    }

    /**
     * Sets new target save file preference.
     *
     * @param save the desired save file
     */
    public void setSaveFile(File save) {
        assert save != null;
        Preferences.userNodeForPackage(PrefsManager.class).put(PREF_NODE_NAME_SAVE_FILE, save.getPath());
        EventManager.getInstance().post(new SaveFileChangedEvent(getSaveFile()));
    }

    /**
     * Clears the current preferred save file path.
     */
    public void clearSaveFilePath() {
        Preferences.userNodeForPackage(PrefsManager.class).remove(PREF_NODE_NAME_SAVE_FILE);
        EventManager.getInstance().post(new SaveFileChangedEvent(null));
    }


    /**
     * @return the current mirror file preference or the default temp file if there is no recorded preference.
     */
    public File getMirrorFile() {
        final String filePath = Preferences.userNodeForPackage(PrefsManager.class).get(PREF_NODE_NAME_MIRROR_FILE, null);
        if (filePath == null) {
            return new File(DEFAULT_TEMP_FILE_PATH);
        } else {
            return new File(filePath);
        }
    }

    /**
     * Sets new mirror file preference.
     *
     * @param mirror the desired mirror file
     */
    public void setMirrorFile(File mirror) {
        assert mirror != null;
        Preferences.userNodeForPackage(PrefsManager.class).put(PREF_NODE_NAME_SAVE_FILE, mirror.getPath());
        // TODO some kind of new mirror file event
    }

    /**
     * Clears current preferred mirror file path.
     */
    public void clearMirrorFilePath() {
        Preferences.userNodeForPackage(PrefsManager.class).remove(PREF_NODE_NAME_MIRROR_FILE);
    }
}
