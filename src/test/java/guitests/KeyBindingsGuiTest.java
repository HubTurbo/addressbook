package guitests;

import address.keybindings.Bindings;
import address.model.datatypes.AddressBook;
import address.model.datatypes.ReadOnlyAddressBook;
import address.model.datatypes.person.Person;
import address.util.TestUtil;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.TagPersonDialogHandle;
import org.junit.Test;

import static org.junit.Assert.*;
import address.sync.cloud.model.CloudAddressBook;
import address.sync.cloud.model.CloudPerson;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests key bindings through the GUI
 */
public class KeyBindingsGuiTest extends GuiTestBase {

    private AddressBook initialData = generateInitialData();

    private AddressBook generateInitialData() {//TODO: create a better set of sample data
        AddressBook ab = new AddressBook();
        ab.addPerson(new Person("Person1", "Lastname1", 1));
        ab.addPerson(new Person("Person2", "Lastname2", 2));
        ab.addPerson(new Person("Person3", "Lastname3", 3));
        ab.addPerson(new Person("Person4", "Lastname4", 4));
        ab.addPerson(new Person("Person5", "Lastname5", 5));
        return ab;
    }

    @Override
    protected ReadOnlyAddressBook getInitialData() {
        return initialData;
    }

    @Override
    protected CloudAddressBook getInitialCloudData() {
        return TestUtil.generateCloudAddressBook(initialData);
    }

    @Test
    public void keyBindings() {

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

        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(4);

        EditPersonDialogHandle editPersonDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        assertEquals("Person4 Lastname4", editPersonDialog.getFullName());
        editPersonDialog.clickCancel();

        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        assertTrue(personListPanel.contains("Person4", "Lastname4")); // still in the list due to grace period
        personListPanel.waitForGracePeriodToExpire();
        assertFalse(personListPanel.contains("Person4", "Lastname4")); // removed from list after grace period

        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(3);

        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        assertTrue(personListPanel.contains("Person3", "Lastname3")); // still in the list due to grace period
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        personListPanel.waitForGracePeriodToExpire();
        assertTrue(personListPanel.contains("Person3", "Lastname3")); // still in the list even after grace period

        TagPersonDialogHandle tagPersonDialog = personListPanel.use_PERSON_TAG_ACCELERATOR();
        tagPersonDialog.close();

        //======== others ============================

        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(2);
        assertTrue(personListPanel.isSelected("Person2", "Lastname2"));

        personListPanel.navigateDown();
        assertTrue(personListPanel.isSelected("Person3", "Lastname3"));

        personListPanel.navigateUp();
        assertTrue(personListPanel.isSelected("Person2", "Lastname2"));

        //======== hotkeys ============================

        mainGui.use_APP_MINIMIZE_HOTKEY();
        assertTrue(mainGui.isMinimized());

        mainGui.use_APP_RESIZE_HOTKEY(); //maximize the window
        assertTrue(mainGui.isMaximized());

        mainGui.use_APP_RESIZE_HOTKEY(); //set window to default size
        assertTrue(mainGui.isDefaultSize());

    }


}
