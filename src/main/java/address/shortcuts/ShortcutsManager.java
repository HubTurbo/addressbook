package address.shortcuts;

import address.events.BaseEvent;
import address.events.EventManager;
import address.events.PotentialKeyboardShortcutEvent;
import address.main.ComponentManager;
import com.google.common.eventbus.Subscribe;
import com.tulskiy.keymaster.common.Provider;

import javax.swing.*;
import java.util.Optional;

/**
 * Manages keyboard shortcuts
 */
public class ShortcutsManager extends ComponentManager{

    /** Provider for global hotkeys */
    private final Provider provider = Provider.getCurrentProvider(false);

    public static Bindings BINDINGS = new Bindings();


    public ShortcutsManager(EventManager eventsManager) {
        super(eventsManager);
        initGlobalHotkeys();
    }

    private void initGlobalHotkeys() {
        for (GlobalHotkey hk: BINDINGS.getHotkeys()){
            provider.register(KeyStroke.getKeyStroke(hk.getHotkeyString()),
                    (hotkey) -> raise(hk.getEventToRaise()));
        }
    }

    @Subscribe
    public void handlePotentialKeyboardShortcutEvent(PotentialKeyboardShortcutEvent potentialKeyboardShortcutEvent) {

        Optional<BaseEvent> eventToRaise = BINDINGS.getEventToRaiseForShortcut(potentialKeyboardShortcutEvent.keyEvent);
        if (eventToRaise.isPresent()) {
            raise(eventToRaise.get());
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
