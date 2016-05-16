package address.shortcuts;

import address.events.DeleteRequestEvent;
import address.events.EventManager;
import address.events.PotentialKeyboardShortcutEvent;
import com.google.common.eventbus.Subscribe;
import javafx.scene.input.KeyEvent;

/**
 * Manages keyboard shortcuts
 */
public class ShortcutsManager {


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
