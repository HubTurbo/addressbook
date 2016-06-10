package address.shortcuts;

import address.events.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Contains the mapping of shortcuts and the corresponding event to raise
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

        SEQUENCE_GOTO_BOTTOM = setSequence(KeyCode.G, KeyCode.B, new JumpToListRequestEvent(-1));
        SEQUENCE_GOTO_TOP = setSequence(KeyCode.G, KeyCode.T, new JumpToListRequestEvent(1));

        ACCELERATOR_FILE_NEW = setAccelerator(KeyCode.N, KeyCombination.SHORTCUT_DOWN);

        ACCELERATOR_FILE_OPEN = setAccelerator(KeyCode.O, KeyCombination.SHORTCUT_DOWN);

        ACCELERATOR_FILE_SAVE = setAccelerator(KeyCode.S, KeyCombination.SHORTCUT_DOWN);

        ACCELERATOR_FILE_SAVE_AS = setAccelerator(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN);

        HOTKEY_APP_MINIMIZE.add(setHotkey("control alt X", new MinimizeAppRequestEvent()));
        HOTKEY_APP_MINIMIZE.add(setHotkey("meta alt X", new MinimizeAppRequestEvent()));

        HOTKEY_APP_MAXIMIZE.add(setHotkey("control shift X", new MaximizeAppRequestEvent()));
        HOTKEY_APP_MAXIMIZE.add(setHotkey("meta shift X", new MaximizeAppRequestEvent()));

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
     * Adds the shortcut to the list of shortcuts.
     * No action will be taken for this shortcut (suitable for shortcuts already used as an accelerator).
     * @param mainKey
     * @param modifierKey
     * @return corresponding key combination
     */
    private Shortcut setShortcut(KeyCode mainKey, KeyCombination.Modifier... modifierKey) {
        KeyCodeCombination keyCodeCombination = new KeyCodeCombination(mainKey, modifierKey);
        Shortcut s = new Shortcut(keyCodeCombination);
        shortcuts.add(s);
        return s;
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
     * @param firstKey
     * @param secondKey
     * @param eventToRaise
     * @return the created object.
     */
    private KeySequence setSequence(KeyCode firstKey, KeyCode secondKey, BaseEvent eventToRaise) {
        KeySequence sq = new KeySequence(firstKey, secondKey, eventToRaise);
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
     * @param hotkeyString
     * @param eventToRaise
     * @return the created object.
     */
    private GlobalHotkey setHotkey(String hotkeyString, BaseEvent eventToRaise) {
        GlobalHotkey hk = new GlobalHotkey(hotkeyString, eventToRaise);
        hotkeys.add(hk);
        return hk;
    }

    /**
     * @param keyEvent
     * @return the Shortcut that matches the keyEvent, if any.
     */
    protected Optional<BaseEvent> getEventToRaiseForShortcut(KeyEvent keyEvent) {
        Optional<Shortcut> matchingShortcut =
                shortcuts.stream()
                        .filter(shortcut -> shortcut.getKeyCombination().match(keyEvent))
                        .findFirst();
        return Optional.ofNullable(matchingShortcut.isPresent() ? matchingShortcut.get().getEventToRaise() : null);
    }

    /**
     * Returns the matching key sequence, if any
     * @param firstKey
     * @param secondKey
     */
    protected Optional<KeySequence> getSequence(KeyCode firstKey, KeyCode secondKey) {
        return sequences.stream()
                .filter(sq -> sq.firstKey == firstKey && sq.secondKey == secondKey)
                .findFirst();
    }


    protected List<GlobalHotkey> getHotkeys() {
        return hotkeys;
    }
}
