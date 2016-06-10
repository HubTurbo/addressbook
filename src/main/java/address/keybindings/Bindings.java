package address.keybindings;

import address.events.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

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


    /* Global hotkeys in alphabetical order of name */
    public List<GlobalHotkey> HOTKEY_APP_MAXIMIZE = new ArrayList<>();
    public List<GlobalHotkey> HOTKEY_APP_MINIMIZE = new ArrayList<>();

    /* Accelerators in alphabetical order of name */
    public Accelerator ACCELERATOR_FILE_NEW;
    public Accelerator ACCELERATOR_FILE_OPEN;
    public Accelerator ACCELERATOR_FILE_SAVE;
    public Accelerator ACCELERATOR_FILE_SAVE_AS;
    public Accelerator ACCELERATOR_PERSON_DELETE;
    public Accelerator ACCELERATOR_PERSON_EDIT;

    /** Sequences in alphabetical order of name */
    public KeySequence SEQUENCE_GOTO_TOP;
    public KeySequence SEQUENCE_GOTO_BOTTOM;

    /* Shortcuts in alphabetical order of name */
    public Shortcut SHORTCUT_LIST_ENTER;

    public Bindings(){
        init();
    }

    private void init(){

        /*====== bindings A-Z keys (in alphabetical order of main key =====================*/

        ACCELERATOR_PERSON_DELETE = setAccelerator(KeyCode.D);

        ACCELERATOR_PERSON_EDIT = setAccelerator(KeyCode.E);

        SEQUENCE_GOTO_BOTTOM = setSequence(new KeyCodeCombination(KeyCode.G), new KeyCodeCombination(KeyCode.B),
                                           new JumpToListRequestEvent(-1));

        SEQUENCE_GOTO_TOP = setSequence(new KeyCodeCombination(KeyCode.G), new KeyCodeCombination(KeyCode.T),
                                        new JumpToListRequestEvent(1));

        ACCELERATOR_FILE_NEW = setAccelerator(KeyCode.N, KeyCombination.SHORTCUT_DOWN);

        ACCELERATOR_FILE_OPEN = setAccelerator(KeyCode.O, KeyCombination.SHORTCUT_DOWN);

        ACCELERATOR_FILE_SAVE = setAccelerator(KeyCode.S, KeyCombination.SHORTCUT_DOWN);

        ACCELERATOR_FILE_SAVE_AS = setAccelerator(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN);

        HOTKEY_APP_MINIMIZE.add(setHotkey(
                new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN, KeyCodeCombination.ALT_DOWN),
                new MinimizeAppRequestEvent()));
        HOTKEY_APP_MINIMIZE.add(setHotkey(
                new KeyCodeCombination(KeyCode.X, KeyCombination.META_DOWN, KeyCodeCombination.ALT_DOWN),
                new MinimizeAppRequestEvent()));

        HOTKEY_APP_MAXIMIZE.add(setHotkey(
                new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN),
                new MaximizeAppRequestEvent()));
        HOTKEY_APP_MAXIMIZE.add(setHotkey(
                new KeyCodeCombination(KeyCode.X, KeyCombination.META_DOWN, KeyCodeCombination.SHIFT_DOWN),
                new MaximizeAppRequestEvent()));

        /*====== other keys ======================================================*/

        SHORTCUT_LIST_ENTER = setShortcut(KeyCode.DOWN, KeyCombination.SHORTCUT_DOWN, new JumpToListRequestEvent(1));

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


    /**
     * @param keyEvent
     * @return the Shortcut that matches the keyEvent, if any.
     */
    protected Optional<Shortcut> getShortcut(KeyEvent keyEvent) {
        return (Optional<Shortcut>) findMatchingBinding(keyEvent, shortcuts);
    }

    private Optional<? extends KeyBinding> findMatchingBinding(KeyEvent event, List<? extends KeyBinding> list){
        return list.stream()
                .filter(shortcut -> shortcut.getKeyCombination().match(event))
                .findFirst();
    }

    /**
     * Returns the matching key sequence, if any
     * @param currentEvent
     * @param previousEvent
     */
    protected Optional<KeySequence> getSequence(PotentialKeyboardShortcutEvent currentEvent,
                                                PotentialKeyboardShortcutEvent previousEvent) {

        if (previousEvent == null){
            return Optional.empty();
        }

        long elapsedTime = PotentialKeyboardShortcutEvent.elapsedTimeInMilliseconds(previousEvent, currentEvent);

        if (elapsedTime > KeySequence.KEY_SEQUENCE_MAX_DELAY_BETWEEN_KEYS){
            return Optional.empty();
        }

        return sequences.stream()
                .filter(sq -> sq.keyCombination.match(previousEvent.keyEvent)
                              && sq.secondKeyCombination.match(currentEvent.keyEvent))
                .findFirst();
    }

    public Optional<? extends KeyBinding>  getBinding(PotentialKeyboardShortcutEvent current,
                                                      PotentialKeyboardShortcutEvent previous){
        Optional<? extends KeyBinding> matchingBinding;

        matchingBinding = getSequence(current, previous);
        if (matchingBinding.isPresent()) { return matchingBinding; }

        matchingBinding = findMatchingBinding(current.keyEvent, shortcuts);
        if (matchingBinding.isPresent()) { return matchingBinding; }

        matchingBinding = findMatchingBinding(current.keyEvent, hotkeys);
        if (matchingBinding.isPresent()) { return matchingBinding; }

        matchingBinding = findMatchingBinding(current.keyEvent, accelerators);
        if (matchingBinding.isPresent()) { return matchingBinding; }

        return Optional.empty();
    }


    protected List<GlobalHotkey> getHotkeys() {
        return hotkeys;
    }
}
