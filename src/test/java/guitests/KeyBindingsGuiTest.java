package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.TagPersonDialogHandle;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests key bindings through the GUI
 */
public class KeyBindingsGuiTest extends GuiTestBase {

    private AddressBook initialData = generateInitialData();

    private AddressBook generateInitialData() {
        AddressBook ab = new AddressBook();
        ab.addPerson(new Person("John", "Wilson", 1));
        ab.addPerson(new Person("John", "Lennon", 2));
        ab.addPerson(new Person("Allan", "Turing", 3));
        ab.addPerson(new Person("Obama", "Micheal", 4));
        ab.addPerson(new Person("Pedo", "Lee", 5));
        return ab;
    }

    @Override
    protected AddressBook getInitialData() {
        return initialData;
    }

    @Test
    public void keyBindings() {

        //======= shortcuts =======================

        personListPanel.use_LIST_ENTER_SHORTCUT();
        assertTrue(personListPanel.isSelected("John", "Wilson"));

        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(4);
        assertTrue(personListPanel.isSelected("Obama", "Micheal"));

        //======= sequences =========================

        personListPanel.use_LIST_GOTO_BOTTOM_SEQUENCE();
        assertTrue(personListPanel.isSelected("Pedo", "Lee"));

        personListPanel.use_LIST_GOTO_TOP_SEQUENCE();
        assertTrue(personListPanel.isSelected("John", "Wilson"));


        //======= accelerators =======================

        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(4);

        EditPersonDialogHandle editPersonDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        assertEquals("Obama Micheal", editPersonDialog.getFullName());
        editPersonDialog.clickCancel();

        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        assertTrue(personListPanel.contains("Obama", "Micheal")); // still in the list due to grace period
        personListPanel.waitForGracePeriodToExpire();
        assertFalse(personListPanel.contains("Obama", "Micheal")); // removed from list after grace period

        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(3);

        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        assertTrue(personListPanel.contains("Allan", "Turing")); // still in the list due to grace period
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        personListPanel.waitForGracePeriodToExpire();
        assertTrue(personListPanel.contains("Allan", "Turing")); // still in the list even after grace period

        TagPersonDialogHandle tagPersonDialog = personListPanel.use_PERSON_TAG_ACCELERATOR();
        tagPersonDialog.close();

        //======== others ============================

        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(2);
        assertTrue(personListPanel.isSelected("John", "Lennon"));

        personListPanel.navigateDown();
        assertTrue(personListPanel.isSelected("Allan", "Turing"));

        personListPanel.navigateUp();
        assertTrue(personListPanel.isSelected("John", "Lennon"));

        //======== hotkeys ============================

        mainGui.use_APP_MINIMIZE_HOTKEY();
        assertTrue(mainGui.isMinimized());

        mainGui.use_APP_RESIZE_HOTKEY(); // un-minimize window
        assertFalse(mainGui.isMinimized());

        mainGui.use_APP_RESIZE_HOTKEY(); // maximize the window
        assertTrue(mainGui.isMaximized());

        mainGui.use_APP_RESIZE_HOTKEY(); // set window to default size
        assertTrue(mainGui.isDefaultSize());
    }


}
