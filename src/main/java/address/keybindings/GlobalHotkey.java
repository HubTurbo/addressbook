package address.keybindings;

import address.events.BaseEvent;

/**
 * Represents a global hotkey.
 */
public class GlobalHotkey {
    private String hotkeyString;
    private BaseEvent eventToRaise;

    GlobalHotkey(String hotkeyString, BaseEvent eventToRaise){
        this.hotkeyString = hotkeyString;
        this.eventToRaise = eventToRaise;
    }

    protected String getHotkeyString() {
        return hotkeyString;
    }

    protected BaseEvent getEventToRaise() {
        return eventToRaise;
    }
}
