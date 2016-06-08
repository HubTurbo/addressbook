package address.shortcuts;

import address.events.BaseEvent;
import address.events.EventManager;
import address.events.PotentialKeyboardShortcutEvent;
import com.google.common.eventbus.Subscribe;
import com.tulskiy.keymaster.common.Provider;

import javax.swing.*;
import java.util.Optional;

/**
 * Manages keyboard shortcuts
 */
public class ShortcutsManager {

    /** Provider for global hotkeys */
    private final Provider provider = Provider.getCurrentProvider(false);

    public static ShortcutsMap SHORTCUTS = new ShortcutsMap();


    public ShortcutsManager() {
        EventManager.getInstance().registerHandler(this);
        initGlobalHotkeys();
    }

    private void initGlobalHotkeys() {
        for (GlobalHotkey hk: SHORTCUTS.getHotkeys()){
            provider.register(KeyStroke.getKeyStroke(hk.getHotkeyString()),
                    (hotkey) -> EventManager.getInstance().post(hk.getEventToRaise()));
        }
    }

    @Subscribe
    public void handlePotentialKeyboardShortcutEvent(PotentialKeyboardShortcutEvent potentialKeyboardShortcutEvent) {

        Optional<BaseEvent> eventToRaise = SHORTCUTS.getEventToRaise(potentialKeyboardShortcutEvent.keyEvent);
        if (eventToRaise.isPresent()) {
            EventManager.getInstance().post(eventToRaise.get());
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
