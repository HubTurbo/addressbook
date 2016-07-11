package address.keybindings;

import address.events.BaseEvent;
import address.events.EventManager;
import address.events.GlobalHotkeyEvent;
import address.util.LoggerManager;
import com.tulskiy.keymaster.common.Provider;
import javafx.scene.input.KeyCodeCombination;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import javax.swing.*;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GlobalHotkeyProviderTest {

    EventManager eventManagerMock = Mockito.mock(EventManager.class);
    GlobalHotkeyProvider globalHotkeyProvider;
    Provider providerMock = Mockito.mock(Provider.class);

    @Before
    public void setup(){
        globalHotkeyProvider = new GlobalHotkeyProvider(eventManagerMock, LoggerManager.getLogger(KeyBindingsManager.class));
        globalHotkeyProvider.provider = providerMock;
    }

    @Test
    public void registerGlobalHotkeys() throws Exception {
        List<GlobalHotkey> hotkeys = new Bindings().getHotkeys();
        globalHotkeyProvider.registerGlobalHotkeys(hotkeys);

        // Verify that the number of hotkeys registered is same as the number of keys in the list
        verify(providerMock, times(hotkeys.size())).register(any(KeyStroke.class), Matchers.any());
    }

    @Test
    public void handleGlobalHotkeyEvent() throws Exception {
        globalHotkeyProvider.handleGlobalHotkeyEvent(new GlobalHotkeyEvent(KeyCodeCombination.valueOf("SHIFT + A")));

        //verify an event was posted
        verify(eventManagerMock, times(1)).post(any(BaseEvent.class));
    }

    @Test
    public void clear() throws Exception {
        globalHotkeyProvider.clear();
        verify(providerMock, times(1)).reset();
        verify(providerMock, times(1)).stop();
    }

}