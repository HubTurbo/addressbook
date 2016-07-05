package guitests;

import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.ManageTagsDialogHandle;
import guitests.guihandles.NewTagDialogHandle;
import guitests.guihandles.TagPersonDialogHandle;
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


        //TODO: convert the code below to use page object pattern

        //Ensure "About" dialog opens
        clickOn("Help").clickOn("About").clickOn("OK");

        //Create a new person Ming Lee, check that last name cannot be blank
        clickOn("New")
                .clickOn("#firstNameField").write("Ming").clickOn("OK")
                .targetWindow("Invalid Fields").clickOn("OK")
                .clickOn("#lastNameField").write("Lee").clickOn("OK");

        //Save file
        clickOn("File").clickOn("[Local] Save");

        //Create a new tag 'company' using the 'Manage Tags' dialog
        ManageTagsDialogHandle manageTagsDialog = mainMenu.clickOn("Tags", "Manage Tags")
                .as(ManageTagsDialogHandle.class);
        newTagDialog = manageTagsDialog.rightClickOn("colleagues").clickOn("New")
                .as(NewTagDialogHandle.class);
        newTagDialog.enterTagName("company");
        newTagDialog.clickOk();
        // assertTrue(manageTagsDialog.contains("company")); //TODO: implement the 'contains' method
        manageTagsDialog.dismiss(); //TODO: this line doesn't seem to work in headless mode


        // UNABLE to launch file chooser in mac's headless mode
        // UNABLE to close tag list dialog in headless mode
    }
}
