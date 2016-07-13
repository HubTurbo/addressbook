package guitests;

import guitests.guihandles.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class FullSystemTest extends GuiTestBase {
    @Test
    public void scenarioOne() {

        //Attempt to create new tag, then cancel
        NewTagDialogHandle newTagDialog = mainMenu.clickOn("Tags", "New Tag")
                                                  .as(NewTagDialogHandle.class);
        assertEquals("", newTagDialog.getTagName());
        newTagDialog.clickCancel();

        //Create a new tag named colleagues using 'New Tag' dialog
        newTagDialog = mainMenu.clickOn("Tags","New Tag")
                               .as(NewTagDialogHandle.class);
        newTagDialog.enterTagName("colleagues");
        assertEquals("colleagues", newTagDialog.getTagName());
        newTagDialog.clickOk();

        //Edit Hans Muster to John Tan, and edit details
        personListPanel.clickOnPerson("Muster");
        //personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(1);
        assertTrue(personListPanel.isSelected("Hans", "Muster"));
        EditPersonDialogHandle editPersonDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        editPersonDialog.enterFirstName("John").enterLastName("Tan")
                .enterCity("Singapore").enterGithubId("john123");
        TagPersonDialogHandle tagPersonDialog = editPersonDialog.openTagPersonDialog();
        tagPersonDialog.enterSearchQuery("coll").acceptSuggestedTag();
        tagPersonDialog.enterSearchQuery("frien").acceptSuggestedTag();
        tagPersonDialog.close();
        assertEquals("John", editPersonDialog.getFirstName());
        assertEquals("Tan", editPersonDialog.getLastName());
        assertEquals("Singapore", editPersonDialog.getCity());
        assertEquals("john123", editPersonDialog.getGithubUserName());
        editPersonDialog.pressEnter();

        //Filter persons list with 'colleagues' tag
        personListPanel.enterFilterAndApply("tag:colleagues");
        assertEquals("tag:colleagues", personListPanel.getFilterText());
        assertTrue(personListPanel.contains("John", "Tan")); // John must be in the filtered list
        assertFalse(personListPanel.contains("Hans", "Muster")); //Hans does not have the 'colleagues' tag

        //Remove filter
        personListPanel.enterFilterAndApply("");

        //Ensure "About" dialog opens
        AboutDialogHandle aboutDialog = mainMenu.clickOn("Help", "About").as(AboutDialogHandle.class);
        aboutDialog.clickOk();

        //Create a new person Ming Lee, check that last name cannot be blank
        EditPersonDialogHandle newPersonDialog = personListPanel.clickNew();
        newPersonDialog.enterFirstName("Ming").clickOk();
        newPersonDialog.dissmissErrorMessage("Invalid Fields");
        newPersonDialog.enterLastName("Lee");
        newPersonDialog.clickOk();
        assertTrue(personListPanel.contains("Ming", "Lee"));

        //Create a new tag 'company' using the 'Manage Tags' dialog
        ManageTagsDialogHandle manageTagsDialog = mainMenu.clickOn("Tags", "Manage Tags")
                .as(ManageTagsDialogHandle.class);
        newTagDialog = manageTagsDialog.rightClickOn("colleagues").clickOn("New")
                .as(NewTagDialogHandle.class);
        newTagDialog.enterTagName("company");
        newTagDialog.clickOk();
        assertTrue(manageTagsDialog.contains("company"));
        manageTagsDialog.dismiss(); //TODO: this line doesn't seem to work in headless mode


        // UNABLE to launch file chooser in mac's headless mode
        // UNABLE to close tag list dialog in headless mode
    }
}
