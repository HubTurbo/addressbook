package address.shortcuts;

import address.events.*;
import com.google.common.eventbus.Subscribe;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.*;

/**
 * Manages keyboard shortcuts
 */
public class ShortcutsManager {

    private static List<Shortcut> shortcuts = new ArrayList<>();

    /* shortcuts in alphabetical order of names */
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

        /*====== other keys ======================================================*/

        SHORTCUT_LIST_ENTER = setShortcut(KeyCode.DOWN, KeyCombination.CONTROL_DOWN,
                ()-> EventManager.getInstance().post(new JumpToListRequestEvent()));
    }

    private static KeyCombination setShortcut(KeyCode mainKey, KeyCombination.Modifier... modifierKey){
        KeyCodeCombination keyCodeCombination = new KeyCodeCombination(mainKey,modifierKey);
        shortcuts.add(new Shortcut(keyCodeCombination, ()->{}));
        return keyCodeCombination;
    }

    private static KeyCodeCombination setShortcut(KeyCode mainKey, KeyCombination.Modifier modifierKey, Runnable action) {
        KeyCodeCombination keyCombination = new KeyCodeCombination(mainKey, modifierKey);
        shortcuts.add(new Shortcut(keyCombination,action));
        return keyCombination;
    }

    private static KeyCodeCombination setShortcut(KeyCode mainKey, Runnable action) {
        KeyCodeCombination keyCombination = new KeyCodeCombination(mainKey);
        shortcuts.add(new Shortcut(keyCombination,action));
        return keyCombination;
    }

    private Optional<Runnable> getAction(KeyEvent keyEvent){
       Optional<Shortcut> matchingShortcut =
               shortcuts
                .stream()
                .filter(shortcut -> shortcut.keyCombination.match(keyEvent))
                .findFirst();
        return Optional.ofNullable(matchingShortcut.isPresent()? matchingShortcut.get().action : null);
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

}
