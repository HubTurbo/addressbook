package guitests;

import address.model.datatypes.AddressBook;
import address.testutil.TestUtil;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.TagPersonDialogHandle;
import javafx.stage.Screen;
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
        assertTrue(personListPanel.isSelected(td.alice));

        personListPanel.clickOnListView();

        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(4);
        assertTrue(personListPanel.isSelected(td.dan));

        //======= sequences =========================

        personListPanel.use_LIST_GOTO_BOTTOM_SEQUENCE();
        assertTrue(personListPanel.isSelected(td.elizabeth));

        personListPanel.use_LIST_GOTO_TOP_SEQUENCE();
        assertTrue(personListPanel.isSelected(td.alice));


        //======= accelerators =======================

        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(4);

        EditPersonDialogHandle editPersonDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        assertEquals("Dan Edwards", editPersonDialog.getFullName());
        editPersonDialog.clickCancel();

        mainGui.focusOnMainApp();
        personListPanel.clickOnPerson(td.dan);

        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        assertTrue(personListPanel.contains("Dan", "Edwards")); // still in the list due to grace period
        mainGui.sleepForGracePeriod();
        assertFalse(personListPanel.contains("Dan", "Edwards")); // removed from list after grace period

        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(3);

        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        assertTrue(personListPanel.contains("Charlie", "Davidson")); // still in the list due to grace period
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        mainGui.sleepForGracePeriod();
        assertTrue(personListPanel.contains("Charlie", "Davidson")); // still in the list even after grace period

        TagPersonDialogHandle tagPersonDialog = personListPanel.use_PERSON_TAG_ACCELERATOR();
        tagPersonDialog.close();

        mainGui.focusOnMainApp();
        personListPanel.clickOnListView();

        //======== others ============================
        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(2);
        assertTrue(personListPanel.isSelected("Benson", "Christopher Dean"));

        personListPanel.navigateDown();
        assertTrue(personListPanel.isSelected("Charlie", "Davidson"));

        personListPanel.navigateUp();
        assertTrue(personListPanel.isSelected("Benson", "Christopher Dean"));

    }

    /**
     * Tests the hotkeys of the application
     * Doesn't work in headfull travis ci.
     */
    @Test
    public void testHotKeys() {
        personListPanel.clickOnListView();

        mainGui.use_APP_MINIMIZE_HOTKEY();
        assertTrue(mainGui.isMinimized());

        mainGui.use_APP_RESIZE_HOTKEY(); // max window
        assertTrue(mainGui.isDefaultSize()); // mainGui.isMinimized() gives wrong result in travis headfull

        mainGui.use_APP_RESIZE_HOTKEY(); // set window to default size
        assertTrue(mainGui.isMaximized());
    }


}
