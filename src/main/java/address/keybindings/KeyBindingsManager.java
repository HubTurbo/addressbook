package address.keybindings;

import address.events.BaseEvent;
import address.events.KeyBindingEvent;
import address.main.ComponentManager;
import address.util.AppLogger;
import address.util.LoggerManager;
import com.google.common.eventbus.Subscribe;

import java.util.Optional;

/**
 * Manages key bindings.
 */
public class KeyBindingsManager extends ComponentManager{

    private static final AppLogger logger = LoggerManager.getLogger(KeyBindingsManager.class);

    /** Manages global hotkey detection */
    private GlobalHotkeyProvider hotkeyProvider = new GlobalHotkeyProvider(eventManager);

    /** To keep track of the previous keyboard event, to match for key sequences */
    private KeyBindingEvent previousKeyEvent = null;

    public static Bindings BINDINGS;

    /**
     * Creates an instance and initializes key bindings (i.e. ready for detection and handling)
     */
    public KeyBindingsManager() {
        super();
        BINDINGS = new Bindings();
        hotkeyProvider.registerGlobalHotkeys(BINDINGS.getHotkeys());
    }

    @Subscribe
    public void handleKeyBindingEvent(KeyBindingEvent currentKeyEvent) {

        Optional<? extends KeyBinding> kb = BINDINGS.getBinding(currentKeyEvent, previousKeyEvent);
        previousKeyEvent = currentKeyEvent;

        if (!kb.isPresent()) {
            logger.debug("Not a recognized key binding: {}", currentKeyEvent);
            return;
        }

        Optional<BaseEvent> event = kb.get().getEventToRaise();
        if (event.isPresent()){
            logger.info("Handling {}", kb.get());
            raise (event.get());
        } else {
            logger.info("Not raising an event because it is handled elsewhere: {}", kb.get());
        }
    }

    /**
     * Resets global hotkeys
     */
    public void clear() {
        hotkeyProvider.clear();
    }
}
