package address.keybindings;


import address.events.*;
import address.testutil.TestUtil;
import javafx.scene.input.KeyCombination;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

public class KeyBindingsManagerApiTest {

    EventManager eventManagerSpy;
    KeyBindingsManagerEx keyBindingsManager;
    List<KeyBinding> yetToTest;

    @Before
    public void setup(){
        eventManagerSpy = Mockito.mock(EventManager.class);
        keyBindingsManager = new KeyBindingsManagerEx();
        keyBindingsManager.setEventManager(eventManagerSpy);
    }

    @After
    public void tearDown() throws Exception {
        keyBindingsManager.stop();
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

        verifyHotkey(new MinimizeAppRequestEvent(), "CTRL + ALT + X");
        verifyHotkey(new MinimizeAppRequestEvent(), "META + ALT + X");
        verifyHotkey(new ResizeAppRequestEvent(), "CTRL + SHIFT + X");
        verifyHotkey(new ResizeAppRequestEvent(), "META + SHIFT + X");

        verifyAccelerator("PERSON_CANCEL_COMMAND_ACCELERATOR", "SHORTCUT + Z");
        verifyAccelerator("PERSON_RETRY_FAILED_COMMAND_ACCELERATOR", "SHORTCUT + Y");
        verifyAccelerator("HELP_PAGE_ACCELERATOR", "F1");
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

        //simulate receiving the key events that matches the key binding
        simulateReceivingKeyBindingEvents(keyCombination);

        //verify that the correct event was raised
        Mockito.verify(eventManagerSpy, times(1)).post((BaseEvent) argThat(new EventIntentionMatcher(expectedEvent)));

        //make a record that the key binding was tested
        markKeyBindingAsTested(keyCombination);
    }

    private void verifyIgnored(String... keyCombination) {

        //As the mocked object is reused multiple times within a single @Test, we reset the mock before each sub test
        Mockito.reset(eventManagerSpy);

        //simulate receiving key events that matches the key binding
        simulateReceivingKeyBindingEvents(keyCombination);

        //verify that no other event was raised i.e. only the simulated key event was posted to event manager
        Mockito.verify(eventManagerSpy, times(0)).post(Matchers.any());
    }

    /**
     * Simulates receiving 1 or 2 key binding events from the event handling mechanism
     * @param keyCombination
     */
    private void simulateReceivingKeyBindingEvents(String[] keyCombination) {
        assert keyCombination.length == 1 || keyCombination.length == 2;

        Arrays.stream(keyCombination)
                .forEach(kc -> keyBindingsManager.handleKeyBindingEvent(new KeyBindingEvent(TestUtil.getKeyEvent(kc))));
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
            //Note: previousEvent should be created first so that it's time stamp is earlier than currentEvent's
            previousEvent = new KeyBindingEvent(TestUtil.getKeyEvent(keyCombos[0]));
            currentEvent = new KeyBindingEvent(TestUtil.getKeyEvent(keyCombos[1]));
        }else {
            previousEvent = null;
            currentEvent = new KeyBindingEvent(TestUtil.getKeyEvent(keyCombos[0]));
        }

        Optional<? extends KeyBinding> tested = keyBindingsManager.getBinding(previousEvent, currentEvent);
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

    @Test
    public void stop(){
        //Not tested here because it is hard to veryify the effect of this method on jkeymaster
        // without being able to fire hotkeys
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
