package guitests;

import guitests.guihandles.ManageTagsDialogHandle;
import guitests.guihandles.NewTagDialogHandle;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TagEditDialogTest extends GuiTestBase {
    @Test
    public void editTagDialogTest() {
        ManageTagsDialogHandle handle = mainMenu.clickOn("Tags", "Manage Tags").as(ManageTagsDialogHandle.class);
        handle.openEditTagDialog("friends");
        assertEquals(handle.getEditTagDialogText(), "friends");

        handle.changeEditTagDialogText("family");
        mainGui.sleep(1, TimeUnit.SECONDS);
        assertEquals(handle.getEditTagDialogText(), "family");
        handle.clickOk();
        assertTrue(handle.contains("family"));

    }

    @Test
    public void tagEditDialogLoadTest() {
        NewTagDialogHandle newTagDialogHandle = mainMenu.clickOn("Tags", "New Tag").as(NewTagDialogHandle.class);
        assertEquals(newTagDialogHandle.getTagName(), "");
        newTagDialogHandle.clickCancel();
        ManageTagsDialogHandle manageTagsDialogHandle = mainMenu.clickOn("Tags", "Manage Tags")
                                                                .as(ManageTagsDialogHandle.class);
        assertTrue(manageTagsDialogHandle.contains("friends"));
        manageTagsDialogHandle.openEditTagDialog("friends");
        assertEquals(manageTagsDialogHandle.getEditTagDialogText(), "friends");
    }

    @Test
    public void updateTagTest() {
        ManageTagsDialogHandle manageTagsDialogHandle = mainMenu.clickOn("Tags", "Manage Tags")
                                                                .as(ManageTagsDialogHandle.class);
        manageTagsDialogHandle.openEditTagDialog("friends");
        manageTagsDialogHandle.changeEditTagDialogText("changed tag");
        manageTagsDialogHandle.clickOk();
        assertTrue(manageTagsDialogHandle.contains("changed tag"));
    }

    @Test
    public void updateTagTest_enterTagEsc_dialogClosed() {
        ManageTagsDialogHandle manageTagsDialogHandle = mainMenu.clickOn("Tags", "Manage Tags")
                .as(ManageTagsDialogHandle.class);
        manageTagsDialogHandle.openEditTagDialog("friends");
        manageTagsDialogHandle.dismiss();
        assertFalse(manageTagsDialogHandle.isChangeEditTagDialogOpen());
    }

}
