package address.prefs;

import address.events.EventManager;
import address.events.MirrorLocationChangedEvent;
import address.events.SaveLocationChangedEvent;

import java.util.prefs.Preferences;
import java.io.File;

/**
 * Manages saving/retrieving of preferences on the user level.
 * Publicly accessible singleton class.
 */
public class PrefsManager {

    public static final String SAVE_LOC_PREF_KEY = "save-location";
    public static final String MIRROR_LOC_PREF_KEY = "mirror-location";

    public static final String DEFAULT_TEMP_FILE_PATH = ".$TEMP_ADDRESS_BOOK";

    private static PrefsManager instance;
    private static Preferences userPrefs = Preferences.userNodeForPackage(PrefsManager.class);

    public static PrefsManager getInstance(){
        if (instance == null){
            instance = new PrefsManager();
        }
        return instance;
    }

    private PrefsManager() {}

    /**
     * @return the current save file preference or the default temp file if there is no recorded preference.
     */
    public File getSaveLocation() {
        final String filePath = userPrefs.get(SAVE_LOC_PREF_KEY, null);
        return filePath == null ? new File(DEFAULT_TEMP_FILE_PATH) : new File(filePath);
    }

    public boolean isSaveLocationSet() {
        return userPrefs.get(SAVE_LOC_PREF_KEY, null) != null;
    }

    /**
     * Sets new target save file preference.
     *
     * @param save the desired save file
     */
    public void setSaveLocation(File save) {
        assert save != null;
        userPrefs.put(SAVE_LOC_PREF_KEY, save.getPath());
        EventManager.getInstance().post(new SaveLocationChangedEvent(getSaveLocation()));
    }

    /**
     * Clears the current preferred save file path.
     */
    public void clearSaveLocation() {
        userPrefs.remove(SAVE_LOC_PREF_KEY);
        EventManager.getInstance().post(new SaveLocationChangedEvent(null));
    }

    /**
     * @return the current mirror file preference or the default temp file if there is no recorded preference.
     */
    public File getMirrorLocation() {
        final String filePath = userPrefs.get(MIRROR_LOC_PREF_KEY, null);
        return filePath == null ? new File(DEFAULT_TEMP_FILE_PATH) : new File(filePath);
    }

    public boolean isMirrorLocationSet() {
        return userPrefs.get(MIRROR_LOC_PREF_KEY, null) != null;
    }

    /**
     * Sets new mirror file preference.
     *
     * @param mirror the desired mirror file
     */
    public void setMirrorLocation(File mirror) {
        assert mirror != null;
        userPrefs.put(MIRROR_LOC_PREF_KEY, mirror.getPath());
        EventManager.getInstance().post(new MirrorLocationChangedEvent(getMirrorLocation()));
    }

    /**
     * Clears current preferred mirror file path.
     */
    public void clearMirrorLocation() {
        userPrefs.remove(MIRROR_LOC_PREF_KEY);
        EventManager.getInstance().post(new MirrorLocationChangedEvent(null));
    }
}
