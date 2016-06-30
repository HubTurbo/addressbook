package address.keybindings;

import address.events.*;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Contains the mapping of key bindings and the corresponding event to raise
 */
public class Bindings {

    /**
     * List of accelerators used.
     * They are here for the purpose of record keeping. Handled automatically by JavaFX.
     */
    private List<Accelerator> accelerators = new ArrayList<>();

    /**
     * List of global hotkeys.
     */
    private List<GlobalHotkey> hotkeys = new ArrayList<>();

    /** List of key sequences */
    private List<KeySequence> sequences = new ArrayList<>();

    /**
     * List of keyboard shortcuts.
     */
    private List<Shortcut> shortcuts = new ArrayList<>();


    /* key bindings in alphabetical order of name */
    public List<GlobalHotkey> APP_MAXIMIZE_HOTKEY = new ArrayList<>();
    public List<GlobalHotkey> APP_MINIMIZE_HOTKEY = new ArrayList<>();
    public Accelerator FILE_NEW_ACCELERATOR;
    public Accelerator FILE_OPEN_ACCELERATOR;
    public Accelerator FILE_SAVE_ACCELERATOR;
    public Accelerator FILE_SAVE_AS_ACCELERATOR;
    public KeySequence LIST_GOTO_TOP_SEQUENCE;
    public KeySequence LIST_GOTO_BOTTOM_SEQUENCE;
    public Shortcut LIST_ENTER_SHORTCUT;
    public Accelerator PERSON_CHANGE_CANCEL_ACCELERATOR;
    public Accelerator PERSON_DELETE_ACCELERATOR;
    public Accelerator PERSON_EDIT_ACCELERATOR;
    public Accelerator PERSON_TAG_ACCELERATOR;


    public Bindings(){
        init();
    }

    private void init(){

        /*====== bindings A-Z keys (in alphabetical order of main key =====================*/

        PERSON_TAG_ACCELERATOR = setAccelerator("PERSON_TAG_ACCELERATOR", "A");
        PERSON_DELETE_ACCELERATOR = setAccelerator("PERSON_DELETE_ACCELERATOR", "D");
        PERSON_EDIT_ACCELERATOR = setAccelerator("PERSON_EDIT_ACCELERATOR", "E");
        LIST_GOTO_BOTTOM_SEQUENCE = setSequence("LIST_GOTO_BOTTOM_SEQUENCE", "G", "B", new JumpToListRequestEvent(-1));
        LIST_GOTO_TOP_SEQUENCE = setSequence("LIST_GOTO_TOP_SEQUENCE", "G", "T", new JumpToListRequestEvent(1));
        FILE_NEW_ACCELERATOR = setAccelerator("FILE_NEW_ACCELERATOR", "SHORTCUT + N");
        FILE_OPEN_ACCELERATOR = setAccelerator("FILE_OPEN_ACCELERATOR", "SHORTCUT + O");
        FILE_SAVE_ACCELERATOR = setAccelerator("FILE_SAVE_ACCELERATOR", "SHORTCUT + S");
        FILE_SAVE_AS_ACCELERATOR = setAccelerator("FILE_SAVE_AS_ACCELERATOR", "SHORTCUT + ALT + S");
        APP_MINIMIZE_HOTKEY.add(setHotkey("APP_MINIMIZE_HOTKEY", "CTRL + ALT + X", new MinimizeAppRequestEvent()));
        APP_MINIMIZE_HOTKEY.add(setHotkey("APP_MINIMIZE_HOTKEY", "META + ALT + X", new MinimizeAppRequestEvent()));
        APP_MAXIMIZE_HOTKEY.add(setHotkey("APP_MAXIMIZE_HOTKEY", "CTRL + SHIFT + X", new MaximizeAppRequestEvent()));
        APP_MAXIMIZE_HOTKEY.add(setHotkey("APP_MAXIMIZE_HOTKEY", "META + SHIFT + X", new MaximizeAppRequestEvent()));
        PERSON_CHANGE_CANCEL_ACCELERATOR = setAccelerator("PERSON_CHANGE_CANCEL_ACCELERATOR", "SHORTCUT + Z");

        /*====== other keys ======================================================*/

        LIST_ENTER_SHORTCUT = setShortcut("LIST_ENTER_SHORTCUT", "SHORTCUT + DOWN", new JumpToListRequestEvent(1));

        //shortcuts for jumping to Nth item in the list n=1..9
        setShortcut("LIST_JUMP_TO_1_SHORTCUT", "SHORTCUT + 1", new JumpToListRequestEvent(1));
        setShortcut("LIST_JUMP_TO_2_SHORTCUT", "SHORTCUT + 2", new JumpToListRequestEvent(2));
        setShortcut("LIST_JUMP_TO_3_SHORTCUT", "SHORTCUT + 3", new JumpToListRequestEvent(3));
        setShortcut("LIST_JUMP_TO_4_SHORTCUT", "SHORTCUT + 4", new JumpToListRequestEvent(4));
        setShortcut("LIST_JUMP_TO_5_SHORTCUT", "SHORTCUT + 5", new JumpToListRequestEvent(5));
        setShortcut("LIST_JUMP_TO_6_SHORTCUT", "SHORTCUT + 6", new JumpToListRequestEvent(6));
        setShortcut("LIST_JUMP_TO_7_SHORTCUT", "SHORTCUT + 7", new JumpToListRequestEvent(7));
        setShortcut("LIST_JUMP_TO_8_SHORTCUT", "SHORTCUT + 8", new JumpToListRequestEvent(8));
        setShortcut("LIST_JUMP_TO_9_SHORTCUT", "SHORTCUT + 9", new JumpToListRequestEvent(9));

    }

    /**
     * Creates a new {@link Accelerator} object and adds it to the list of accelerators.
     * @param name
     * @param keyCombination
     * @return the created object.
     */
    private Accelerator setAccelerator(String name, String keyCombination) {
        Accelerator a = new Accelerator(name, KeyCodeCombination.valueOf(keyCombination));
        accelerators.add(a);
        return a;
    }


    /**
     * Creates a new {@link KeySequence} object and adds it to the list of key sequences.
     * @param name
     * @param firstKeyCombination
     * @param secondKeyCombination
     * @param eventToRaise
     * @return the created object.
     */
    private KeySequence setSequence(String name, String firstKeyCombination,
                                    String secondKeyCombination, BaseEvent eventToRaise) {

        KeySequence sq = new KeySequence(name, KeyCodeCombination.valueOf(firstKeyCombination),
                                         KeyCodeCombination.valueOf(secondKeyCombination), eventToRaise);
        sequences.add(sq);
        return sq;
    }
    /**
     * Creates a new {@link Shortcut} object and adds to the list of shortcuts.
     * @param name
     * @param keyCombination
     * @param eventToRaise
     * @return the created object.
     */
    private Shortcut setShortcut(String name, String keyCombination, BaseEvent eventToRaise) {
        Shortcut s = new Shortcut(name, KeyCombination.valueOf(keyCombination), eventToRaise);
        shortcuts.add(s);
        return s;
    }

    /**
     * Creates a new {@link GlobalHotkey} object and adds it to the hotkey list.
     * @param name
     * @param keyCombination
     * @param eventToRaise
     * @return the created object.
     */
    private GlobalHotkey setHotkey(String name, String keyCombination, BaseEvent eventToRaise) {
        GlobalHotkey hk = new GlobalHotkey(name, KeyCombination.valueOf(keyCombination), eventToRaise);
        hotkeys.add(hk);
        return hk;
    }


    /**
     * Finds a matching key binding, if any.
     * @param keyBindingEvent
     * @param list list of key bindings to search
     * @return
     */
    private Optional<? extends KeyBinding> findMatchingBinding(KeyBindingEvent keyBindingEvent,
                                                               List<? extends KeyBinding> list){
        return list.stream()
                .filter(shortcut -> keyBindingEvent.isMatching(shortcut.getKeyCombination()))
                .findFirst();
    }

    private Optional<GlobalHotkey> findMatchingHotkey(KeyBindingEvent keyboardShortcutEvent){
        return hotkeys.stream()
                .filter(shortcut -> keyboardShortcutEvent.isMatching(shortcut.getKeyCombination()))
                .findFirst();
    }

    /**
     * Returns the matching key sequence, if any
     * @param currentEvent
     * @param previousEvent
     */
    protected Optional<KeySequence> findMatchingSequence(KeyBindingEvent currentEvent,
                                                         KeyBindingEvent previousEvent) {

        if (previousEvent == null){
            return Optional.empty();
        }

        long elapsedTime = KeyBindingEvent.elapsedTimeInMilliseconds(previousEvent, currentEvent);

        if (elapsedTime > KeySequence.KEY_SEQUENCE_MAX_DELAY_BETWEEN_KEYS){
            return Optional.empty();
        }

        return sequences.stream()
                .filter(sq -> previousEvent.isMatching(sq.keyCombination)
                              && currentEvent.isMatching(sq.secondKeyCombination))
                .findFirst();
    }

    /**
     * @param current the key event being matched
     * @param previous the previous key event (this is needed to match for key sequences)
     * @return the matching key binding, if any
     */
    public Optional<? extends KeyBinding>  getBinding(KeyBindingEvent current,
                                                      KeyBindingEvent previous){
        Optional<? extends KeyBinding> matchingBinding;

        matchingBinding = findMatchingSequence(current, previous);
        if (matchingBinding.isPresent()) { return matchingBinding; }

        matchingBinding = findMatchingHotkey(current);
        if (matchingBinding.isPresent()) { return matchingBinding; }

        matchingBinding = findMatchingBinding(current, shortcuts);
        if (matchingBinding.isPresent()) { return matchingBinding; }

        matchingBinding = findMatchingBinding(current, accelerators);
        if (matchingBinding.isPresent()) { return matchingBinding; }

        return Optional.empty();
    }


    protected List<GlobalHotkey> getHotkeys() {
        return hotkeys;
    }

    public List<Accelerator> getAccelerators() {return accelerators; }

    public List<KeySequence> getSequences() { return sequences; }

    /**
     * Returns a list of all {@link KeyBinding} objects being managed.
     */
    public List<KeyBinding> getAllBindings(){
        List<KeyBinding> all = new ArrayList<>();
        all.addAll(accelerators);
        all.addAll(shortcuts);
        all.addAll(sequences);
        all.addAll(hotkeys);
        return all;
    }
}
