package address.keybindings;

import address.events.*;
import address.events.hotkey.AcceleratorIgnoredEvent;
import address.events.hotkey.KeyBindingEvent;
import javafx.scene.input.KeyCodeCombination;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class KeyBindingsManagerTest {
    KeyBindingsManager keyBindingsManager;
    EventManager eventManagerMock;

    @Before
    public void setUp() throws Exception {
        keyBindingsManager = new KeyBindingsManager();
        eventManagerMock = Mockito.mock(EventManager.class);
        keyBindingsManager.setEventManager(eventManagerMock);
        Bindings bindingsMock = Mockito.mock(Bindings.class);
        keyBindingsManager.BINDINGS = bindingsMock;
        keyBindingsManager.hotkeyProvider = Mockito.mock(GlobalHotkeyProvider.class);
    }

    @After
    public void tearDown()throws Exception{
        keyBindingsManager.stop();
    }

    @Test
    public void handleKeyBindingEvent_nonExistentKeyBinding_eventNotRaised() throws Exception {
        //Set up mock to return nothing
        doReturn(Optional.empty()).when(keyBindingsManager.BINDINGS).getBinding(anyObject(), anyObject());

        //Call SUT with non-existent keybinding event
        keyBindingsManager.handleKeyBindingEvent(new KeyBindingEvent(KeyCodeCombination.valueOf("N")));

        //Verify event is not called
        verify(eventManagerMock, times(0)).post(any());
    }

    @Test
    public void handleKeyBindingEvent_existingKeyBinding_eventRaised() throws Exception {
        //Set up mock to return a known accelerator
        KeyBinding deleteAccelerator = new Bindings().PERSON_DELETE_ACCELERATOR;
        doReturn(Optional.of(deleteAccelerator)).when(keyBindingsManager.BINDINGS).getBinding(anyObject(), anyObject());

        //Invoke SUT
        keyBindingsManager.handleKeyBindingEvent(new KeyBindingEvent(deleteAccelerator.keyCombination));

        //Verify the event was raised
        verify(eventManagerMock, times(1)).post(Matchers.isA(AcceleratorIgnoredEvent.class));
    }

    @Test
    public void stop() throws Exception {
        keyBindingsManager.stop();
        verify(keyBindingsManager.hotkeyProvider, times(1)).clear();
    }

    @Test
    public void getAcceleratorKeyCombo() throws Exception {
        // Set up the mock to return a list containing two accelerators
        ArrayList<Accelerator> accelerators = new ArrayList<>();
        Accelerator personEditAccelerator = new Bindings().PERSON_EDIT_ACCELERATOR;
        accelerators.add(personEditAccelerator);
        accelerators.add(new Bindings().PERSON_CANCEL_COMMAND_ACCELERATOR);

        // Verify checking for non-existent accelerator
        assertEquals(Optional.empty(), keyBindingsManager.getAcceleratorKeyCombo("non existent"));

        // Verify checking for existing accelerator
        when(keyBindingsManager.BINDINGS.getAccelerators()).thenReturn(accelerators);
        assertEquals(personEditAccelerator.getKeyCombination(),
                     keyBindingsManager.getAcceleratorKeyCombo("PERSON_EDIT_ACCELERATOR").get());
    }

}