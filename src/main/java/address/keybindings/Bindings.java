package address.keybindings;

import address.events.*;
import javafx.scene.input.KeyCode;
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

        PERSON_DELETE_ACCELERATOR = setAccelerator(KeyCode.D);

        PERSON_EDIT_ACCELERATOR = setAccelerator(KeyCode.E);

        LIST_GOTO_BOTTOM_SEQUENCE = setSequence(new KeyCodeCombination(KeyCode.G), new KeyCodeCombination(KeyCode.B),
                                           new JumpToListRequestEvent(-1));

        LIST_GOTO_TOP_SEQUENCE = setSequence(new KeyCodeCombination(KeyCode.G), new KeyCodeCombination(KeyCode.T),
                                        new JumpToListRequestEvent(1));

        FILE_NEW_ACCELERATOR = setAccelerator(KeyCode.N, KeyCombination.SHORTCUT_DOWN);

        FILE_OPEN_ACCELERATOR = setAccelerator(KeyCode.O, KeyCombination.SHORTCUT_DOWN);

        FILE_SAVE_ACCELERATOR = setAccelerator(KeyCode.S, KeyCombination.SHORTCUT_DOWN);

        FILE_SAVE_AS_ACCELERATOR = setAccelerator(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN);

        APP_MINIMIZE_HOTKEY.add(setHotkey(
                new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN, KeyCodeCombination.ALT_DOWN),
                new MinimizeAppRequestEvent()));
        APP_MINIMIZE_HOTKEY.add(setHotkey(
                new KeyCodeCombination(KeyCode.X, KeyCombination.META_DOWN, KeyCodeCombination.ALT_DOWN),
                new MinimizeAppRequestEvent()));

        APP_MAXIMIZE_HOTKEY.add(setHotkey(
                new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN),
                new MaximizeAppRequestEvent()));
        APP_MAXIMIZE_HOTKEY.add(setHotkey(
                new KeyCodeCombination(KeyCode.X, KeyCombination.META_DOWN, KeyCodeCombination.SHIFT_DOWN),
                new MaximizeAppRequestEvent()));

        /*====== other keys ======================================================*/

        LIST_ENTER_SHORTCUT = setShortcut(KeyCode.DOWN, KeyCombination.SHORTCUT_DOWN, new JumpToListRequestEvent(1));

        //shortcuts for jumping to Nth item in the list n=1..9
        setShortcut(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN, new JumpToListRequestEvent(1));
        setShortcut(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN, new JumpToListRequestEvent(2));
        setShortcut(KeyCode.DIGIT3, KeyCombination.SHORTCUT_DOWN, new JumpToListRequestEvent(3));
        setShortcut(KeyCode.DIGIT4, KeyCombination.SHORTCUT_DOWN, new JumpToListRequestEvent(4));
        setShortcut(KeyCode.DIGIT5, KeyCombination.SHORTCUT_DOWN, new JumpToListRequestEvent(5));
        setShortcut(KeyCode.DIGIT6, KeyCombination.SHORTCUT_DOWN, new JumpToListRequestEvent(6));
        setShortcut(KeyCode.DIGIT7, KeyCombination.SHORTCUT_DOWN, new JumpToListRequestEvent(7));
        setShortcut(KeyCode.DIGIT8, KeyCombination.SHORTCUT_DOWN, new JumpToListRequestEvent(8));
        setShortcut(KeyCode.DIGIT9, KeyCombination.SHORTCUT_DOWN, new JumpToListRequestEvent(9));

    }

    /**
     * Creates a new {@link Accelerator} object and adds it to the list of accelerators.
     * @param mainKey
     * @param modifierKey
     * @return the created object.
     */
    private Accelerator setAccelerator(KeyCode mainKey, KeyCombination.Modifier... modifierKey) {
        KeyCodeCombination keyCodeCombination = new KeyCodeCombination(mainKey, modifierKey);
        Accelerator a = new Accelerator(keyCodeCombination);
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
    private KeySequence setSequence(KeyCombination firstKeyCombination, KeyCombination secondKeyCombination,
                                    BaseEvent eventToRaise) {
        KeySequence sq = new KeySequence(firstKeyCombination, secondKeyCombination, eventToRaise);
        sequences.add(sq);
        return sq;
    }
    /**
     * Creates a new {@link Shortcut} object and adds to the list of shortcuts.
     * @param mainKey
     * @param modifierKey
     * @param eventToRaise
     * @return the created object.
     */
    private Shortcut setShortcut(KeyCode mainKey, KeyCombination.Modifier modifierKey, BaseEvent eventToRaise) {
        KeyCodeCombination keyCombination = new KeyCodeCombination(mainKey, modifierKey);
        Shortcut s = new Shortcut(keyCombination, eventToRaise);
        shortcuts.add(s);
        return s;
    }

    /**
     * Creates a new {@link GlobalHotkey} object and adds it to the list.
     * @param keyCombination
     * @param eventToRaise
     * @return the created object.
     */
    private GlobalHotkey setHotkey(KeyCombination keyCombination, BaseEvent eventToRaise) {
        GlobalHotkey hk = new GlobalHotkey(keyCombination, eventToRaise);
        hotkeys.add(hk);
        return hk;
    }


    private Optional<? extends KeyBinding> findMatchingBinding(PotentialKeyboardShortcutEvent keyboardShortcutEvent,
                                                               List<? extends KeyBinding> list){
        return list.stream()
                .filter(shortcut -> keyboardShortcutEvent.isMatching(shortcut.getKeyCombination()))
                .findFirst();
    }

    private Optional<GlobalHotkey> findMatchingHotkey(PotentialKeyboardShortcutEvent keyboardShortcutEvent){
        return hotkeys.stream()
                .filter(shortcut -> keyboardShortcutEvent.isMatching(shortcut.getKeyCombination()))
                .findFirst();
    }

    /**
     * Returns the matching key sequence, if any
     * @param currentEvent
     * @param previousEvent
     */
    protected Optional<KeySequence> findMatchingSequence(PotentialKeyboardShortcutEvent currentEvent,
                                                         PotentialKeyboardShortcutEvent previousEvent) {

        if (previousEvent == null){
            return Optional.empty();
        }

        long elapsedTime = PotentialKeyboardShortcutEvent.elapsedTimeInMilliseconds(previousEvent, currentEvent);

        if (elapsedTime > KeySequence.KEY_SEQUENCE_MAX_DELAY_BETWEEN_KEYS){
            return Optional.empty();
        }

        return sequences.stream()
                .filter(sq -> previousEvent.isMatching(sq.keyCombination)
                              && currentEvent.isMatching(sq.secondKeyCombination))
                .findFirst();
    }

    public Optional<? extends KeyBinding>  getBinding(PotentialKeyboardShortcutEvent current,
                                                      PotentialKeyboardShortcutEvent previous){
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
