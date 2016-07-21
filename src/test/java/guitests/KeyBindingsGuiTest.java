package guitests;

import address.model.datatypes.AddressBook;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.TagPersonDialogHandle;
import org.junit.Test;


import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Tests key bindings through the GUI
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
        assertTrue(personListPanel.isSelected("Alice", "Brown"));

        personListPanel.clickOnListView();

        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(4);
        assertTrue(personListPanel.isSelected("Dan", "Edwards"));

        //======= sequences =========================

        personListPanel.use_LIST_GOTO_BOTTOM_SEQUENCE();
        assertTrue(personListPanel.isSelected("Elizabeth", "F. Green"));

        personListPanel.use_LIST_GOTO_TOP_SEQUENCE();
        assertTrue(personListPanel.isSelected("Alice", "Brown"));


        //======= accelerators =======================

        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(4);

        EditPersonDialogHandle editPersonDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        assertEquals("Dan Edwards", editPersonDialog.getFullName());
        editPersonDialog.clickCancel();

        personListPanel.focusOnMainApp();
        personListPanel.clickOnListView();

        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        assertTrue(personListPanel.contains("Dan", "Edwards")); // still in the list due to grace period
        personListPanel.waitForGracePeriodToExpire();
        assertFalse(personListPanel.contains("Dan", "Edwards")); // removed from list after grace period

        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(3);

        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        assertTrue(personListPanel.contains("Charlie", "Davidson")); // still in the list due to grace period
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        personListPanel.waitForGracePeriodToExpire();
        assertTrue(personListPanel.contains("Charlie", "Davidson")); // still in the list even after grace period

        TagPersonDialogHandle tagPersonDialog = personListPanel.use_PERSON_TAG_ACCELERATOR();
        tagPersonDialog.close();

        personListPanel.focusOnMainApp();
        personListPanel.clickOnListView();

        //======== others ============================
        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(2);
        assertTrue(personListPanel.isSelected("Benson", "Christopher Dean"));

        personListPanel.navigateDown();
        assertTrue(personListPanel.isSelected("Charlie", "Davidson"));

        personListPanel.navigateUp();
        assertTrue(personListPanel.isSelected("Benson", "Christopher Dean"));

        //======== hotkeys ============================

        mainGui.focusOnMainApp();
        personListPanel.clickOnListView();

        mainGui.use_APP_MINIMIZE_HOTKEY();
        mainGui.sleep(2, TimeUnit.SECONDS);
        assertTrue(mainGui.isMinimized());

/*
        mainGui.use_APP_RESIZE_HOTKEY(); // un-minimize window
        mainGui.sleep(2, TimeUnit.SECONDS);
        assertFalse(mainGui.isMinimized());
*/

        mainGui.use_APP_RESIZE_HOTKEY(); // maximize the window
        mainGui.sleep(2, TimeUnit.SECONDS);
        assertTrue(mainGui.isMaximized());

        mainGui.use_APP_RESIZE_HOTKEY(); // set window to default size
        mainGui.sleep(2, TimeUnit.SECONDS);
        assertTrue(mainGui.isDefaultSize());
    }


}
