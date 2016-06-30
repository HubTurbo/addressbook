package address.keybindings;

import address.events.AcceleratorIgnoredEvent;
import address.events.BaseEvent;
import address.events.KeyBindingEvent;
import address.main.ComponentManager;
import address.util.AppLogger;
import address.util.LoggerManager;
import com.google.common.eventbus.Subscribe;
import javafx.scene.input.KeyCombination;

import java.util.List;
import java.util.Optional;

/**
 * Manages key bindings.
 */
public class KeyBindingsManager extends ComponentManager{

    private static final AppLogger logger = LoggerManager.getLogger(KeyBindingsManager.class);

    /** Manages global hotkey detection */
    private GlobalHotkeyProvider hotkeyProvider = new GlobalHotkeyProvider(eventManager, logger);

    /** To keep track of the previous keyboard event, to match for key sequences */
    private KeyBindingEvent previousKeyEvent = null;

    protected static Bindings BINDINGS;

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
            logger.debug("Not a recognized key binding: {} ", currentKeyEvent);
            return;
        }

        logger.info("Handling {}", kb.get());
        BaseEvent event = kb.get().getEventToRaise();
        raise (event);

    }

    /**
     * Resets global hotkeys
     */
    public void stop() {
        hotkeyProvider.clear();
    }



    /**
     * Returns the key combination of the accelerator matching the name given.
     */
    public static Optional<KeyCombination> getAcceleratorKeyCombo(String name) {
        Optional<? extends KeyBinding> keyBinding =
                BINDINGS.getAccelerators().stream()
                .filter(kb -> kb.getName().equals(name))
                .findFirst();
        return keyBinding.isPresent() ? Optional.of(keyBinding.get().getKeyCombination()) : Optional.empty();
    }


}
