package address.shortcuts;

import address.events.DeleteRequestEvent;
import address.events.EventManager;
import address.events.PotentialKeyboardShortcutEvent;
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

    @Subscribe
    public void handlePotentialKeyboardShortcutEvent(PotentialKeyboardShortcutEvent potentialKeyboardShortcutEvent) {
        switch (potentialKeyboardShortcutEvent.keyEvent.getCode()) {
            case D:
                EventManager.getInstance().post(new DeleteRequestEvent()); break;
            default:
                System.out.println("Unknown shortcut");
        }
    }
}
