package address.shortcuts;

import address.events.*;
import com.google.common.eventbus.Subscribe;
import com.tulskiy.keymaster.common.Provider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import javax.swing.*;
import java.util.*;

/**
 * Manages keyboard shortcuts
 */
public class ShortcutsManager {

    /** Provider for global hotkeys */
    private final Provider provider = Provider.getCurrentProvider(false);

    private static List<Shortcut> shortcuts = new ArrayList<>();
    private static List<GlobalHotkey> hotkeys = new ArrayList<>();

    /* shortcuts in alphabetical order of names */
    public static final List<String> HOTKEY_APP_MINIMIZE = new ArrayList<>();
    public static final List<String> HOTKEY_APP_MAXIMIZE = new ArrayList<>();
    public static final KeyCombination SHORTCUT_FILE_NEW;
    public static final KeyCombination SHORTCUT_FILE_OPEN;
    public static final KeyCombination SHORTCUT_FILE_SAVE;
    public static final KeyCombination SHORTCUT_FILE_SAVE_AS;
    public static final KeyCombination SHORTCUT_LIST_ENTER;
    public static final KeyCombination SHORTCUT_PERSON_DELETE;
    public static final KeyCombination SHORTCUT_PERSON_EDIT;

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

        SHORTCUT_LIST_ENTER = setShortcut(KeyCode.DOWN, KeyCombination.CONTROL_DOWN,
                () -> EventManager.getInstance().post(new JumpToListRequestEvent(1)));

        //shortcuts for jumping to Nth item in the list n=1..9
        setShortcut(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN,
                () -> EventManager.getInstance().post(new JumpToListRequestEvent(1)));
        setShortcut(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN,
                () -> EventManager.getInstance().post(new JumpToListRequestEvent(2)));
        setShortcut(KeyCode.DIGIT3, KeyCombination.CONTROL_DOWN,
                () -> EventManager.getInstance().post(new JumpToListRequestEvent(3)));
        setShortcut(KeyCode.DIGIT4, KeyCombination.CONTROL_DOWN,
                () -> EventManager.getInstance().post(new JumpToListRequestEvent(4)));
        setShortcut(KeyCode.DIGIT5, KeyCombination.CONTROL_DOWN,
                () -> EventManager.getInstance().post(new JumpToListRequestEvent(5)));
        setShortcut(KeyCode.DIGIT6, KeyCombination.CONTROL_DOWN,
                () -> EventManager.getInstance().post(new JumpToListRequestEvent(6)));
        setShortcut(KeyCode.DIGIT7, KeyCombination.CONTROL_DOWN,
                () -> EventManager.getInstance().post(new JumpToListRequestEvent(7)));
        setShortcut(KeyCode.DIGIT8, KeyCombination.CONTROL_DOWN,
                () -> EventManager.getInstance().post(new JumpToListRequestEvent(8)));
        setShortcut(KeyCode.DIGIT9, KeyCombination.CONTROL_DOWN,
                () -> EventManager.getInstance().post(new JumpToListRequestEvent(9)));

    }

    public ShortcutsManager() {
        EventManager.getInstance().registerHandler(this);
        initGlobalHotkeys();
    }

    private void initGlobalHotkeys() {
        for (GlobalHotkey hk: hotkeys){
            provider.register(KeyStroke.getKeyStroke(hk.hotkeyString),
                    (hotkey) -> EventManager.getInstance().post(hk.eventToRaise));
        }
    }

    /**
     * Adds the shortcut to the list of shortcuts.
     * No action will be taken for this shortcut (suitable for shortcuts already used as an accelerator).
     * @param mainKey
     * @param modifierKey
     * @return corresponding key combination
     */
    private static KeyCombination setShortcut(KeyCode mainKey, KeyCombination.Modifier... modifierKey) {
        KeyCodeCombination keyCodeCombination = new KeyCodeCombination(mainKey, modifierKey);
        shortcuts.add(new Shortcut(keyCodeCombination, () -> {}));
        return keyCodeCombination;
    }

    /**
     * Adds the shortcut to the list of shortcuts.
     * @param mainKey
     * @param modifierKey
     * @param action
     * @return corresponding key combination
     */
    private static KeyCodeCombination setShortcut(KeyCode mainKey, KeyCombination.Modifier modifierKey,
                                                  Runnable action) {
        KeyCodeCombination keyCombination = new KeyCodeCombination(mainKey, modifierKey);
        shortcuts.add(new Shortcut(keyCombination, action));
        return keyCombination;
    }

    private static String setHotkey(String hotkeyString, BaseEvent eventToRaise) {
        hotkeys.add(new GlobalHotkey(hotkeyString, eventToRaise));
        return hotkeyString;
    }


    /**
     * @param keyEvent
     * @return the Shortcut that matches the keyEvent
     */
    private Optional<Runnable> getAction(KeyEvent keyEvent) {
        Optional<Shortcut> matchingShortcut =
                shortcuts.stream()
                        .filter(shortcut -> shortcut.keyCombination.match(keyEvent))
                        .findFirst();
        return Optional.ofNullable(matchingShortcut.isPresent() ? matchingShortcut.get().action : null);
    }

    @Subscribe
    public void handlePotentialKeyboardShortcutEvent(PotentialKeyboardShortcutEvent potentialKeyboardShortcutEvent) {

        Optional<Runnable> action = getAction(potentialKeyboardShortcutEvent.keyEvent);
        if (action.isPresent()) {
            action.get().run();
        } else {
            System.out.println("No action for shortcut " + potentialKeyboardShortcutEvent.keyEvent);
        }
    }

    /**
     * Resets global hotkeys
     */
    public void clear() {
        provider.reset();
        provider.stop();
    }
}
