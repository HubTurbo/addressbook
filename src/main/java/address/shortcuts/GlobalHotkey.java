package address.shortcuts;

import address.events.BaseEvent;

/**
 * Represents a global hotkey
 */
public class GlobalHotkey {
    String hotkeyString;
    BaseEvent eventToRaise;

    GlobalHotkey(String hotkeyString, BaseEvent eventToRaise){
        this.hotkeyString = hotkeyString;
        this.eventToRaise = eventToRaise;
    }
}
