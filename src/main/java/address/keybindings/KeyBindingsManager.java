package address.keybindings;

import address.events.BaseEvent;
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
public class KeyBindingsManager extends ComponentManager{
    private static final Logger logger = LogManager.getLogger(KeyBindingsManager.class);

    /** Max delay (in milliseconds) allowed between key presses of a key sequence */
    private static final int KEY_SEQUENCE_MAX_DELAY_BETWEEN_KEYS = 1000;

    /** Provider for global hotkeys */
    private final Provider provider = Provider.getCurrentProvider(false);

    private PotentialKeyboardShortcutEvent previousKeyEvent = null;

    public static Bindings BINDINGS;


    public KeyBindingsManager() {
        super();
        BINDINGS = new Bindings();
        registerGlobalHotkeys(BINDINGS.getHotkeys());
    }

    private void registerGlobalHotkeys(List<GlobalHotkey> hotkeys) {
        for (GlobalHotkey hk: hotkeys){
            provider.register(hk.getKeyStroke(), (hotkey) -> raise(hk.getEventToRaise()));
        }
    }

    @Subscribe
    public void handlePotentialKeyboardShortcutEvent(PotentialKeyboardShortcutEvent currentKeyEvent) {

        Optional<KeySequence> ks = getKeySequence(previousKeyEvent, currentKeyEvent);
        previousKeyEvent = currentKeyEvent;
        if (ks.isPresent()){
            raise(ks.get().eventToRaise);
            return;
        }

        Optional<BaseEvent> eventToRaise = BINDINGS.getEventToRaiseForShortcut(currentKeyEvent.keyEvent);
        if (eventToRaise.isPresent()) {
            raise(eventToRaise.get());
        } else {
            logger.info("No action for shortcut " + currentKeyEvent.keyEvent);
        }
    }

    /**
     * Returns the matching key sequence, if any.
     * @param previousKeyEvent
     * @param currentEvent
     * @return
     */
    private Optional<KeySequence> getKeySequence(PotentialKeyboardShortcutEvent previousKeyEvent,
                                                 PotentialKeyboardShortcutEvent currentEvent) {

        if (previousKeyEvent == null){
            return Optional.empty();
        }

        long elapsedTime = PotentialKeyboardShortcutEvent.elapsedTimeInMilliseconds(previousKeyEvent, currentEvent);

        if (elapsedTime < KEY_SEQUENCE_MAX_DELAY_BETWEEN_KEYS){
            return BINDINGS.getSequence(previousKeyEvent.keyEvent.getCode(), currentEvent.keyEvent.getCode());
        }

        return Optional.empty();
    }

    /**
     * Resets global hotkeys
     */
    public void clear() {
        provider.reset();
        provider.stop();
    }
}
