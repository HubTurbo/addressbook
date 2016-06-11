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
    public Accelerator PERSON_DELETE_ACCELERATOR;
    public Accelerator PERSON_EDIT_ACCELERATOR;


    public Bindings(){
        init();
    }

    private void init(){

        /*====== bindings A-Z keys (in alphabetical order of main key =====================*/

        PERSON_DELETE_ACCELERATOR = setAccelerator("D");
        PERSON_EDIT_ACCELERATOR = setAccelerator("D");
        LIST_GOTO_BOTTOM_SEQUENCE = setSequence("G", "B", new JumpToListRequestEvent(-1));
        LIST_GOTO_TOP_SEQUENCE = setSequence("G", "T", new JumpToListRequestEvent(1));
        FILE_NEW_ACCELERATOR = setAccelerator("Shortcut + N");
        FILE_OPEN_ACCELERATOR = setAccelerator("Shortcut + O");
        FILE_SAVE_ACCELERATOR = setAccelerator("Shortcut + S");
        FILE_SAVE_AS_ACCELERATOR = setAccelerator("Shortcut + Alt + S");
        APP_MINIMIZE_HOTKEY.add(setHotkey("Ctrl + Alt + X", new MinimizeAppRequestEvent()));
        APP_MINIMIZE_HOTKEY.add(setHotkey("Meta + Alt + X", new MinimizeAppRequestEvent()));
        APP_MAXIMIZE_HOTKEY.add(setHotkey("Ctrl + Shift + X", new MaximizeAppRequestEvent()));
        APP_MAXIMIZE_HOTKEY.add(setHotkey("Meta + Shift + X", new MaximizeAppRequestEvent()));

        /*====== other keys ======================================================*/

        LIST_ENTER_SHORTCUT = setShortcut("Shortcut + Down", new JumpToListRequestEvent(1));

        //shortcuts for jumping to Nth item in the list n=1..9
        setShortcut("Shortcut + 1", new JumpToListRequestEvent(1));
        setShortcut("Shortcut + 2", new JumpToListRequestEvent(2));
        setShortcut("Shortcut + 3", new JumpToListRequestEvent(3));
        setShortcut("Shortcut + 4", new JumpToListRequestEvent(4));
        setShortcut("Shortcut + 5", new JumpToListRequestEvent(5));
        setShortcut("Shortcut + 6", new JumpToListRequestEvent(6));
        setShortcut("Shortcut + 7", new JumpToListRequestEvent(7));
        setShortcut("Shortcut + 8", new JumpToListRequestEvent(8));
        setShortcut("Shortcut + 9", new JumpToListRequestEvent(9));

    }

    /**
     * Creates a new {@link Accelerator} object and adds it to the list of accelerators.
     * @param keyCombination
     * @return the created object.
     */
    private Accelerator setAccelerator(String keyCombination) {
        Accelerator a = new Accelerator(KeyCodeCombination.valueOf(keyCombination));
        accelerators.add(a);
        return a;
    }


    /**
     * Creates a new {@link KeySequence} object and adds it to the list of key sequences.
     * @param firstKeyCombination
     * @param secondKeyCombination
     * @param eventToRaise
     * @return the created object.
     */
    private KeySequence setSequence(String firstKeyCombination, String secondKeyCombination, BaseEvent eventToRaise) {
        KeySequence sq = new KeySequence(KeyCodeCombination.valueOf(firstKeyCombination),
                                         KeyCodeCombination.valueOf(secondKeyCombination),
                                         eventToRaise);
        sequences.add(sq);
        return sq;
    }
    /**
     * Creates a new {@link Shortcut} object and adds to the list of shortcuts.
     * @param keyCombination
     * @param eventToRaise
     * @return the created object.
     */
    private Shortcut setShortcut(String keyCombination, BaseEvent eventToRaise) {
        Shortcut s = new Shortcut(KeyCombination.valueOf(keyCombination), eventToRaise);
        shortcuts.add(s);
        return s;
    }

    /**
     * Creates a new {@link GlobalHotkey} object and adds it to the list.
     * @param keyCombination
     * @param eventToRaise
     * @return the created object.
     */
    private GlobalHotkey setHotkey(String keyCombination, BaseEvent eventToRaise) {
        GlobalHotkey hk = new GlobalHotkey(KeyCombination.valueOf(keyCombination), eventToRaise);
        hotkeys.add(hk);
        return hk;
    }


    private Optional<? extends KeyBinding> findMatchingBinding(KeyBindingEvent keyboardShortcutEvent,
                                                               List<? extends KeyBinding> list){
        return list.stream()
                .filter(shortcut -> keyboardShortcutEvent.isMatching(shortcut.getKeyCombination()))
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
}
