package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import address.testutil.TestUtil;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.TagPersonDialogHandle;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests key bindings through the GUI
 * Special Note:
 * In travis ci headlfull testing, the following were observed:
 * 1) Keybinding tests failed as shortcuts are not triggered.
 *    - First shortcuts get triggered as normal, second shortcuts onwards failed to get triggered.
 * Solution(Not implemented, as travis headfull testing is not needed currently):
 * - Click on the listview first before firing shortcuts. (Use personListPanel.clickOnListView())
 *   (Seems to work, but no concrete reason on why it works).
 */
public class KeyBindingsGuiTest extends GuiTestBase {

    @Override
    protected AddressBook getInitialData() {
        return td.book;
    }

    @Test
    public void keyBindings() {
        //======= shortcuts =======================

        personListPanel.use_LIST_ENTER_SHORTCUT();
        assertTrue(personListPanel.isSelected(td.alice));

        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(4);
        assertTrue(personListPanel.isSelected(td.dan));

        //======= sequences =========================

        personListPanel.use_LIST_GOTO_BOTTOM_SEQUENCE();
        assertTrue(personListPanel.isSelected((Person) TestUtil.getLastElement(td.book.getPersons())));

        personListPanel.use_LIST_GOTO_TOP_SEQUENCE();
        assertTrue(personListPanel.isSelected(td.alice));


        //======= accelerators =======================

        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(4);

        EditPersonDialogHandle editPersonDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        assertEquals("Dan Edwards", editPersonDialog.getFullName());
        editPersonDialog.clickCancel();

        personListPanel.clickOnPerson(td.dan);

        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        assertFalse(personListPanel.contains(td.dan)); // removed from list after grace period

        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(3);

        TagPersonDialogHandle tagPersonDialog = personListPanel.use_PERSON_TAG_ACCELERATOR();
        tagPersonDialog.close();

        //======== others ============================
        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(2);
        assertTrue(personListPanel.isSelected(td.benson));

        personListPanel.navigateDown();
        assertTrue(personListPanel.isSelected(td.charlie));

        personListPanel.navigateUp();
        assertTrue(personListPanel.isSelected(td.benson));

    }

    /**
     * Tests the hotkeys of the application
     * Doesn't work in headfull travis ci.
     */
    @Test
    public void testHotKeys() {

        mainGui.use_APP_MINIMIZE_HOTKEY();
        assertTrue(mainGui.isMinimized());

        mainGui.use_APP_RESIZE_HOTKEY(); // max window
        assertTrue(mainGui.isDefaultSize()); // mainGui.isMinimized() gives wrong result in travis headfull

        mainGui.use_APP_RESIZE_HOTKEY(); // set window to default size
        assertTrue(mainGui.isMaximized());
    }


}
