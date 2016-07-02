package address.keybindings;


import address.events.*;
import address.testutils.TestUtil;
import javafx.scene.input.KeyCombination;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;

public class KeyBindingsManagerApiTest {

    EventManager eventManagerSpy;
    KeyBindingsManagerEx keyBindingsManager;
    List<KeyBinding> yetToTest;

    @Before
    public void setup(){
        eventManagerSpy = Mockito.spy(EventManager.getInstance());
        keyBindingsManager = new KeyBindingsManagerEx();
        keyBindingsManager.setEventManager(eventManagerSpy);
    }

    @Test
    public void keyEventDetected_definedBinding_matchingEventRaised() throws Exception {

        // At the beginning, all known key bindings are yet to be tested.
        //    Each key binding will be removed from the list after it is tested.
        yetToTest = keyBindingsManager.getAllKeyBindings();

        /*====== bindings A-Z keys (in alphabetical order of main key =====================*/

        verifyAccelerator("PERSON_TAG_ACCELERATOR", "A");
        verifyAccelerator("PERSON_DELETE_ACCELERATOR", "D");
        verifyAccelerator("PERSON_EDIT_ACCELERATOR", "E");
        verifySequence(new JumpToListRequestEvent(-1), "G", "B");
        verifySequence(new JumpToListRequestEvent(1), "G", "T");
        verifyAccelerator("FILE_NEW_ACCELERATOR", "SHORTCUT + N");
        verifyAccelerator("FILE_OPEN_ACCELERATOR", "SHORTCUT + O");
        verifyAccelerator("FILE_SAVE_ACCELERATOR", "SHORTCUT + S");
        verifyAccelerator("FILE_SAVE_AS_ACCELERATOR", "SHORTCUT + ALT + S");

        verifyHotkey(new MinimizeAppRequestEvent(), "CTRL + ALT + X");
        verifyHotkey(new MinimizeAppRequestEvent(), "META + ALT + X");
        verifyHotkey(new MaximizeAppRequestEvent(), "CTRL + SHIFT + X");
        verifyHotkey(new MaximizeAppRequestEvent(), "META + SHIFT + X");

        verifyAccelerator("PERSON_CHANGE_CANCEL_ACCELERATOR", "SHORTCUT + Z");
         /*====== other keys ======================================================*/

        verifyShortcut(new JumpToListRequestEvent(1), "SHORTCUT DOWN");

        //verify [SHORTCUT + 1] to [SHORTCUT + 9]
        IntStream.rangeClosed(1, 9)
                .forEach(i -> verifyShortcut(new JumpToListRequestEvent(i), "SHORTCUT DIGIT"+i));

        //Ensure that all key bindings have been tested (in case a future developer added a key binding without tests)
        assertTrue("Some key bindings are not tested : " + yetToTest, yetToTest.isEmpty());
    }

    @Test
    public void keyEventDetected_undefinedBinding_ignored(){
        verifyIgnored("ALT + A");
        verifyIgnored("CTRL + ALT + A");
        verifyIgnored("SHORTCUT + SHIFT + A");
    }

    private void verifyAccelerator(String acceleratorName, String keyCombo) {
        verify(new AcceleratorIgnoredEvent(acceleratorName), keyCombo);
    }

    private void verifyHotkey(BaseEvent expectedEvent, String keyCombo) {
        //Detection of global hotkey by jkeymaster is not tested.
        //  To be tested by system tests.
        verify(expectedEvent, keyCombo);
    }

    private void verifySequence(BaseEvent expectedEvent, String firstKeyCombo, String secondKeyCombo) throws InterruptedException {
        //No special action taken to avoid mix up with previous/next key event because
        // the mocked event manager is reset after each test.
        verify(expectedEvent, firstKeyCombo, secondKeyCombo);
    }

    private void verifyShortcut(BaseEvent expectedEvent, String keyCombo) {
        verify(expectedEvent, keyCombo);
    }

    private void verify(BaseEvent expectedEvent, String... keyCombination) {

        //As the mocked object is reused multiple times within a single @Test, we reset the mock before each sub test
        Mockito.reset(eventManagerSpy);

        //simulate the key events that matches the key binding
        simulateKeyEvents(keyCombination);

        //verify that the simulated key event was detected
        Mockito.verify(eventManagerSpy, times(keyCombination.length)).post(Matchers.isA(KeyBindingEvent.class));

        //verify that the correct event was raised
        Mockito.verify(eventManagerSpy, times(1)).post((BaseEvent) argThat(new EventIntentionMatcher(expectedEvent)));

        //make a record that the key binding was tested
        markKeyBindingAsTested(keyCombination);
    }

    private void verifyIgnored(String... keyCombination) {

        //As the mocked object is reused multiple times within a single @Test, we reset the mock before each sub test
        Mockito.reset(eventManagerSpy);

        //simulate the key events that matches the key binding
        simulateKeyEvents(keyCombination);

        //verify that the simulated key event was detected
        Mockito.verify(eventManagerSpy, times(keyCombination.length)).post(Matchers.isA(KeyBindingEvent.class));

        //verify that no other event was raised i.e. only the simulated key event was posted to event manager
        Mockito.verify(eventManagerSpy, times(keyCombination.length)).post(Matchers.any());
    }

    /**
     * Simulates one or two key combinations being used
     * @param keyCombination
     */
    private void simulateKeyEvents(String[] keyCombination) {
        assert keyCombination.length == 1 || keyCombination.length == 2;
        Arrays.stream(keyCombination)
                .forEach(kc -> eventManagerSpy.post(new KeyBindingEvent(TestUtil.getKeyEvent(kc))));
    }

    /**
     * Removed the matching key binding from the list of key bindings yet to test
     * @param keyCombos
     */
    private void markKeyBindingAsTested(String[] keyCombos) {
        assert keyCombos.length == 1 || keyCombos.length == 2;

        KeyBindingEvent currentEvent;
        KeyBindingEvent previousEvent;

        if(keyCombos.length == 2){
            currentEvent = new KeyBindingEvent(TestUtil.getKeyEvent(keyCombos[1]));
            previousEvent = new KeyBindingEvent(TestUtil.getKeyEvent(keyCombos[0]));
        }else {
            currentEvent = new KeyBindingEvent(TestUtil.getKeyEvent(keyCombos[0]));
            previousEvent = null;
        }

        Optional<? extends KeyBinding> tested = keyBindingsManager.getBinding(currentEvent, previousEvent);
        yetToTest.remove(tested.get());
    }




    @Test
    public void getAcceleratorKeyCombo(){
        //check one existing accelerator
        Assert.assertEquals(keyBindingsManager.getAcceleratorKeyCombo("PERSON_TAG_ACCELERATOR").get(),
                KeyCombination.valueOf("A"));

        //check one existing accelerator
        Assert.assertEquals(keyBindingsManager.getAcceleratorKeyCombo("PERSON_DELETE_ACCELERATOR").get(),
                KeyCombination.valueOf("D"));

        //check one existing accelerator
        Assert.assertEquals(keyBindingsManager.getAcceleratorKeyCombo("PERSON_EDIT_ACCELERATOR").get(),
                KeyCombination.valueOf("E"));

        //check an non-existing accelerator
        assertFalse(keyBindingsManager.getAcceleratorKeyCombo("NON_EXISTENT").isPresent());

        //check an non-existing accelerator that is a shortcut
        assertFalse(keyBindingsManager.getAcceleratorKeyCombo("LIST_ENTER_SHORTCUT").isPresent());
    }

    @Test
    public void ensureNoClashBetweenSequencesAndAccelerators(){
        Bindings bindings = new Bindings();
        List<KeySequence> sequences = bindings.getSequences();
        bindings.getAccelerators().stream()
                .forEach(accelerator -> verifyNotInSequences(sequences, accelerator));
    }

    private void verifyNotInSequences(List<KeySequence> sequences, Accelerator a) {
        Optional<KeySequence> matchingSequence = sequences.stream()
                .filter(s -> s.isIncluded(a.getKeyCombination())).findFirst();
        assertFalse("Clash between " + matchingSequence + " and " + a, matchingSequence.isPresent());
    }

    /**
     * A custom argument matcher used to compare two events to see if they have the same intention.
     */
    class EventIntentionMatcher extends ArgumentMatcher {

        private final BaseEvent expected;

        EventIntentionMatcher(BaseEvent expected){
            this.expected = expected;
        }

        @Override
        public boolean matches(Object actual) {
            return actual instanceof BaseEvent
                    && expected.hasSameIntentionAs((BaseEvent) actual);
        }

    }

}
