package address.prefs;

import address.events.EventManager;
import address.events.SaveLocationChangedEvent;

import java.util.prefs.Preferences;
import java.io.File;

/**
 * Manages saving/retrieving of preferences in the registry.
  */
public class PrefsManager {

    public static final String PREF_NODE_NAME_SAVE_LOC = "save-location";
    public static final String PREF_NODE_NAME_MIRROR_LOC = "mirror-location";

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
    public File getSaveLocation() {
        final String filePath = Preferences.userNodeForPackage(PrefsManager.class).get(PREF_NODE_NAME_SAVE_LOC, null);
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
    public void setSaveLocation(File save) {
        assert save != null;
        Preferences.userNodeForPackage(PrefsManager.class).put(PREF_NODE_NAME_SAVE_LOC, save.getPath());
        EventManager.getInstance().post(new SaveLocationChangedEvent(getSaveLocation()));
    }

    /**
     * Clears the current preferred save file path.
     */
    public void clearSaveLocation() {
        Preferences.userNodeForPackage(PrefsManager.class).remove(PREF_NODE_NAME_SAVE_LOC);
        EventManager.getInstance().post(new SaveLocationChangedEvent(null));
    }


    /**
     * @return the current mirror file preference or the default temp file if there is no recorded preference.
     */
    public File getMirrorLocation() {
        final String filePath = Preferences.userNodeForPackage(PrefsManager.class).get(PREF_NODE_NAME_MIRROR_LOC, null);
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
    public void setMirrorLocation(File mirror) {
        assert mirror != null;
        Preferences.userNodeForPackage(PrefsManager.class).put(PREF_NODE_NAME_SAVE_LOC, mirror.getPath());
        // TODO some kind of new mirror file event
    }

    /**
     * Clears current preferred mirror file path.
     */
    public void clearMirrorLocation() {
        Preferences.userNodeForPackage(PrefsManager.class).remove(PREF_NODE_NAME_MIRROR_LOC);
    }
}
