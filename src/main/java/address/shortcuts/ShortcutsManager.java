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

    public static final KeyCombination SHORTCUT_FILE_NEW = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
    public static final KeyCombination SHORTCUT_FILE_OPEN = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);
    public static final KeyCombination SHORTCUT_FILE_SAVE = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
    public static final KeyCombination SHORTCUT_FILE_SAVE_AS = new KeyCodeCombination(KeyCode.S,
                                                                            KeyCombination.CONTROL_DOWN,
                                                                            KeyCombination.ALT_DOWN);
    public static final KeyCombination SHORTCUT_PERSON_EDIT = new KeyCodeCombination(KeyCode.T);
    public static final KeyCombination SHORTCUT_JUMP_TO_LIST = new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN);

    private List<Shortcut> shortcuts = new ArrayList<>();

    public ShortcutsManager(){
        initShortcuts();
    }

    private void initShortcuts(){
        addShortcut(KeyCode.D,
                ()-> EventManager.getInstance().post(new DeleteRequestEvent()));
        addShortcut(KeyCode.E,
                ()-> EventManager.getInstance().post(new EditRequestEvent()));
        addShortcut(KeyCode.DOWN, KeyCombination.CONTROL_DOWN,
                ()-> EventManager.getInstance().post(new JumpToListRequestEvent()));

    }

    private void addShortcut(KeyCode mainKey, KeyCombination.Modifier modifierKey, Runnable action) {
        shortcuts.add(new Shortcut(new KeyCodeCombination(mainKey, modifierKey),action));
    }

    private void addShortcut(KeyCode mainKey, Runnable action) {
        shortcuts.add(new Shortcut(new KeyCodeCombination(mainKey),action));
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

    private class Shortcut{
        KeyCombination keyCombination;
        Runnable action;

        Shortcut(KeyCombination keyCombination, Runnable action){
            this.keyCombination = keyCombination;
            this.action = action;
        }
    }

}
