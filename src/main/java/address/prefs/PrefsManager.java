package address.prefs;

import address.events.EventManager;
import address.events.SaveLocationChangedEvent;

import java.util.prefs.Preferences;
import java.io.File;

/**
 * Manages saving/retrieving of preferences on the user level.
 * Publicly accessible singleton class.
 */
public class PrefsManager {

    public static final String SAVE_LOC_PREF_KEY = "save-location";


    private static PrefsManager instance;
    private static Preferences prefStorage = Preferences.userNodeForPackage(PrefsManager.class);

    private UserPrefs prefs;

    public static PrefsManager getInstance(){
        if (instance == null){
            instance = new PrefsManager();
        }
        return instance;
    }

    private PrefsManager() {
        prefs = new UserPrefs();
        prefs.saveLocation = prefStorage.get(SAVE_LOC_PREF_KEY, null);
    }

    public UserPrefs getPrefs(){
        return prefs;
    }

    public boolean isSaveLocationSet() {
        return prefStorage.get(SAVE_LOC_PREF_KEY, null) != null;
    }

    /**
     * Sets new target save file preference.
     *
     * @param save the desired save file
     */
    public void setSaveLocation(File save) {
        assert save != null;
        prefStorage.put(SAVE_LOC_PREF_KEY, save.getPath());
        prefs.saveLocation = save.getPath();
        EventManager.getInstance().post(new SaveLocationChangedEvent(prefs.getSaveLocation()));
    }

    /**
     * Clears the current preferred save file path.
     */
    public void clearSaveLocation() {
        prefStorage.remove(SAVE_LOC_PREF_KEY);
        prefs.saveLocation = null;
        EventManager.getInstance().post(new SaveLocationChangedEvent(null));
    }
}
