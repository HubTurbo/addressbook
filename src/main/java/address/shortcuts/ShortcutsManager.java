package address.shortcuts;

import address.events.BaseEvent;
import address.events.EventManager;
import address.events.PotentialKeyboardShortcutEvent;
import address.main.ComponentManager;
import com.google.common.eventbus.Subscribe;
import com.tulskiy.keymaster.common.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

/**
 * Manages key bindings.
 */
public class ShortcutsManager extends ComponentManager{
    private static final Logger logger = LogManager.getLogger(ShortcutsManager.class);

    /** Provider for global hotkeys */
    private final Provider provider = Provider.getCurrentProvider(false);

    public static Bindings BINDINGS;


    public ShortcutsManager() {
        super();
        BINDINGS = new Bindings();
        registerGlobalHotkeys(BINDINGS.getHotkeys());
    }

    private void registerGlobalHotkeys(List<GlobalHotkey> hotkesy) {
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
            logger.info("No action for shortcut " + potentialKeyboardShortcutEvent.keyEvent);
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
