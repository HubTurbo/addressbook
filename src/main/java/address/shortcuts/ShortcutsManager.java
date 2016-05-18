package address.shortcuts;

import address.events.*;
import com.google.common.eventbus.Subscribe;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

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

    @Subscribe
    public void handlePotentialKeyboardShortcutEvent(PotentialKeyboardShortcutEvent potentialKeyboardShortcutEvent) {

        if(SHORTCUT_JUMP_TO_LIST.match(potentialKeyboardShortcutEvent.keyEvent)){
            EventManager.getInstance().post(new JumpToListRequestEvent());
            return;
        }

        switch (potentialKeyboardShortcutEvent.keyEvent.getCode()) {
            case D:
                EventManager.getInstance().post(new DeleteRequestEvent()); break;
            case E:
                EventManager.getInstance().post(new EditRequestEvent()); break;
            default:
                System.out.println("Unknown shortcut");
        }
    }
}
