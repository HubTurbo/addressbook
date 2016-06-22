package address.prefs;

import address.events.EventManager;
import address.events.SaveLocationChangedEvent;
import address.util.AppLogger;
import address.util.LoggerManager;

import java.io.IOException;
import java.util.prefs.Preferences;
import java.io.File;

/**
 * Manages saving/retrieving of preferences on the user level.
 * Publicly accessible singleton class.
 */
public class PrefsManager {
    private static final AppLogger logger = LoggerManager.getLogger(PrefsManager.class);

    private static PrefsManager instance;

    private UserPrefs prefs;

    public static PrefsManager getInstance(){
        if (instance == null){
            instance = new PrefsManager();
        }
        return instance;
    }

    private PrefsManager() {
        try {
            prefs = UserPrefs.readPrefFromFile();
        } catch (IOException e) {
            logger.info("Failed to read UserPrefs from file; using empty UserPrefs.", e);
            prefs = new UserPrefs();
        }
    }

    public UserPrefs getPrefs(){
        return prefs;
    }

    public boolean isSaveLocationSet() {
        return prefs.getSaveLocation() != null;
    }

    /**
     * Sets new target save file preference.
     *
     * @param save the desired save file
     */
    public void setSaveLocation(File save) {
        assert save != null;
        prefs.setSaveLocation(save.getPath());
        EventManager.getInstance().post(new SaveLocationChangedEvent(prefs.getSaveLocation()));
    }

    /**
     * Clears the current preferred save file path.
     */
    public void clearSaveLocation() {
        prefs.setSaveLocation(null);
        EventManager.getInstance().post(new SaveLocationChangedEvent(null));
    }
}
