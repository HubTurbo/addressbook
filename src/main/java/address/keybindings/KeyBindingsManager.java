package address.keybindings;

import address.events.BaseEvent;
import address.events.GlobalHotkeyEvent;
import address.events.KeyBindingEvent;
import address.main.ComponentManager;
import com.google.common.eventbus.Subscribe;
import com.tulskiy.keymaster.common.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

/**
 * Manages key bindings.
 */
public class KeyBindingsManager extends ComponentManager{

    private static final Logger logger = LogManager.getLogger(KeyBindingsManager.class);

    /** Provider for global hotkeys */
    private final Provider provider = Provider.getCurrentProvider(false);

    /** To keep track of the previous keyboard event, to match for key sequences */
    private KeyBindingEvent previousKeyEvent = null;

    public static Bindings BINDINGS;


    public KeyBindingsManager() {
        super();
        BINDINGS = new Bindings();
        registerGlobalHotkeys(BINDINGS.getHotkeys());
    }

    /**
     * Registers a {@link GlobalHotkeyEvent} with jKeymaster for each global hotkey
     * @param hotkeys
     */
    private void registerGlobalHotkeys(List<GlobalHotkey> hotkeys) {
        for (GlobalHotkey hk: hotkeys){
            provider.register(hk.getKeyStroke(), (hotkey) -> raise(new GlobalHotkeyEvent(hk.keyCombination)));
        }
    }

    /**
     * Swallows the {@link GlobalHotkeyEvent} raised by jKeyMaster and
     * raises the corresponding {@link KeyBindingEvent}
     * @param globalHotkeyEvent
     */
    @Subscribe
    public void handleGlobalHotkeyEvent(GlobalHotkeyEvent globalHotkeyEvent){
        raise(globalHotkeyEvent.keyboardShortcutEvent);
    }

    @Subscribe
    public void handleKeyBindingEvent(KeyBindingEvent currentKeyEvent) {

        Optional<? extends KeyBinding> kb = BINDINGS.getBinding(currentKeyEvent, previousKeyEvent);
        previousKeyEvent = currentKeyEvent;

        if (!kb.isPresent()) {
            logger.debug("Not a recognized key binding : " + currentKeyEvent);
            return;
        }

        Optional<BaseEvent> event = kb.get().getEventToRaise();
        if (event.isPresent()){
            logger.info("Handling " + kb.get());
            raise (event.get());
        } else {
            logger.info("Not raising an event because it is handled elsewhere : " + kb.get());
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
