package address.shortcuts;

import address.events.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Contains the mapping of shortcuts and the corresponding event to raise
 */
public class Bindings {
    private static List<Shortcut> shortcuts = new ArrayList<>();
    private static List<GlobalHotkey> hotkeys = new ArrayList<>();

    /* shortcuts in alphabetical order of names */
    public static final List<GlobalHotkey> HOTKEY_APP_MINIMIZE = new ArrayList<>();
    public static final List<GlobalHotkey> HOTKEY_APP_MAXIMIZE = new ArrayList<>();
    public static final Shortcut SHORTCUT_FILE_NEW;
    public static final Shortcut SHORTCUT_FILE_OPEN;
    public static final Shortcut SHORTCUT_FILE_SAVE;
    public static final Shortcut SHORTCUT_FILE_SAVE_AS;
    public static final Shortcut SHORTCUT_LIST_ENTER;
    public static final Shortcut SHORTCUT_PERSON_DELETE;
    public static final Shortcut SHORTCUT_PERSON_EDIT;

    static {

        /*====== A-Z keys (in alphabetical order of main key =====================*/

        SHORTCUT_PERSON_DELETE = setShortcut(KeyCode.D);

        SHORTCUT_PERSON_EDIT = setShortcut(KeyCode.E);

        SHORTCUT_FILE_NEW = setShortcut(KeyCode.N, KeyCombination.CONTROL_DOWN);

        SHORTCUT_FILE_OPEN = setShortcut(KeyCode.O, KeyCombination.CONTROL_DOWN);

        SHORTCUT_FILE_SAVE = setShortcut(KeyCode.S, KeyCombination.CONTROL_DOWN);

        SHORTCUT_FILE_SAVE_AS = setShortcut(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN);

        HOTKEY_APP_MINIMIZE.add(setHotkey("control alt X", new MinimizeAppRequestEvent()));
        HOTKEY_APP_MINIMIZE.add(setHotkey("meta alt X", new MinimizeAppRequestEvent()));

        HOTKEY_APP_MAXIMIZE.add(setHotkey("control shift X", new MaximizeAppRequestEvent()));
        HOTKEY_APP_MAXIMIZE.add(setHotkey("meta shift X", new MaximizeAppRequestEvent()));

        /*====== other keys ======================================================*/

        SHORTCUT_LIST_ENTER = setShortcut(KeyCode.DOWN, KeyCombination.CONTROL_DOWN, new JumpToListRequestEvent(1));

        //shortcuts for jumping to Nth item in the list n=1..9
        setShortcut(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN, new JumpToListRequestEvent(1));
        setShortcut(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN, new JumpToListRequestEvent(2));
        setShortcut(KeyCode.DIGIT3, KeyCombination.CONTROL_DOWN, new JumpToListRequestEvent(3));
        setShortcut(KeyCode.DIGIT4, KeyCombination.CONTROL_DOWN, new JumpToListRequestEvent(4));
        setShortcut(KeyCode.DIGIT5, KeyCombination.CONTROL_DOWN, new JumpToListRequestEvent(5));
        setShortcut(KeyCode.DIGIT6, KeyCombination.CONTROL_DOWN, new JumpToListRequestEvent(6));
        setShortcut(KeyCode.DIGIT7, KeyCombination.CONTROL_DOWN, new JumpToListRequestEvent(7));
        setShortcut(KeyCode.DIGIT8, KeyCombination.CONTROL_DOWN, new JumpToListRequestEvent(8));
        setShortcut(KeyCode.DIGIT9, KeyCombination.CONTROL_DOWN, new JumpToListRequestEvent(9));

    }
    /**
     * Adds the shortcut to the list of shortcuts.
     * No action will be taken for this shortcut (suitable for shortcuts already used as an accelerator).
     * @param mainKey
     * @param modifierKey
     * @return corresponding key combination
     */
    private static Shortcut setShortcut(KeyCode mainKey, KeyCombination.Modifier... modifierKey) {
        KeyCodeCombination keyCodeCombination = new KeyCodeCombination(mainKey, modifierKey);
        Shortcut s = new Shortcut(keyCodeCombination);
        shortcuts.add(s);
        return s;
    }

    /**
     * Adds the shortcut to the list of shortcuts.
     * @param mainKey
     * @param modifierKey
     * @param eventToRaise
     * @return corresponding key combination
     */
    private static Shortcut setShortcut(KeyCode mainKey, KeyCombination.Modifier modifierKey,
                                        BaseEvent eventToRaise) {
        KeyCodeCombination keyCombination = new KeyCodeCombination(mainKey, modifierKey);
        Shortcut s = new Shortcut(keyCombination, eventToRaise);
        shortcuts.add(s);
        return s;
    }

    private static GlobalHotkey setHotkey(String hotkeyString, BaseEvent eventToRaise) {
        GlobalHotkey hk = new GlobalHotkey(hotkeyString, eventToRaise);
        hotkeys.add(hk);
        return hk;
    }


    /**
     * @param keyEvent
     * @return the Shortcut that matches the keyEvent
     */
    protected Optional<BaseEvent> getEventToRaiseForShortcut(KeyEvent keyEvent) {
        Optional<Shortcut> matchingShortcut =
                shortcuts.stream()
                        .filter(shortcut -> shortcut.getKeyCombination().match(keyEvent))
                        .findFirst();
        return Optional.ofNullable(matchingShortcut.isPresent() ? matchingShortcut.get().getEventToRaise() : null);
    }

    protected List<GlobalHotkey> getHotkeys() {
        return hotkeys;
    }
}
