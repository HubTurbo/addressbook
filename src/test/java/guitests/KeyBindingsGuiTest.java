package guitests;

import address.keybindings.Bindings;
import address.model.datatypes.AddressBook;
import address.model.datatypes.ReadOnlyAddressBook;
import address.model.datatypes.person.Person;
import guitests.guihandles.EditPersonDialogHandle;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests key bindings through the GUI
 */
public class KeyBindingsGuiTest extends GuiTestBase {

    private final Bindings bindings = new Bindings();

    @Override
    protected ReadOnlyAddressBook getInitialData() {
        AddressBook ab = new AddressBook();
        ab.addPerson(new Person("Person1", "Lastname1", 1));
        ab.addPerson(new Person("Person2", "Lastname2", 2));
        ab.addPerson(new Person("Person3", "Lastname3", 3));
        ab.addPerson(new Person("Person4", "Lastname4", 4));
        ab.addPerson(new Person("Person5", "Lastname5", 5));
        return ab;
        //TODO: create a better set of sample data
    }

    @Test
    public void keyBindings(){

        //======= shortcuts =======================

        personListPanel.use_LIST_ENTER_SHORTCUT();
        assertTrue(personListPanel.isSelected("Person1", "Lastname1"));

        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(4);
        assertTrue(personListPanel.isSelected("Person4", "Lastname4"));

        //======= sequences =========================

        personListPanel.use_LIST_GOTO_BOTTOM_SEQUENCE();
        assertTrue(personListPanel.isSelected("Person5", "Lastname5"));

        personListPanel.use_LIST_GOTO_TOP_SEQUENCE();
        assertTrue(personListPanel.isSelected("Person1", "Lastname1"));


        //======= accelerators =======================

        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(3);

        EditPersonDialogHandle editPersonDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        assertEquals("Person3 Lastname3", editPersonDialog.getFullName());
        editPersonDialog.clickCancel();

        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        assertTrue(personListPanel.contains("Person3", "Lastname3")); // still in the list due to grace period
        personListPanel.waitForGracePeriodToExpire();
        assertFalse(personListPanel.contains("Person3", "Lastname3")); // removed from list after grace period

        //TODO: test tag, file open, new, save, save as, cancel

        //======== others ============================

        personListPanel.use_LIST_GOTO_BOTTOM_SEQUENCE();
        personListPanel.navigateUp();

        assertTrue(personListPanel.isSelected("Person4", "Lastname4"));

        personListPanel.navigateDown();
        assertTrue(personListPanel.isSelected("Person5", "Lastname5"));

        //======== hotkeys ============================

        push(bindings.APP_MINIMIZE_HOTKEY.get(0));
        push(bindings.APP_RESIZE_HOTKEY.get(0)); //maximize the window
        push(bindings.APP_RESIZE_HOTKEY.get(0)); //set window to default size
        //TODO: test hotkeys further

    }


}
