package guitests;

import address.model.datatypes.AddressBook;
import address.testutil.ContextMenuChoice;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.HeaderStatusBarHandle;
import guitests.guihandles.PersonCardHandle;
import org.junit.Test;


import java.util.Optional;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

/**
 * System tests for 'New person' feature.
 */
public class PersonNewGuiTest extends GuiTestBase {

    @Override
    protected AddressBook getInitialData() {
        return td.book;
    }

    @Test
    public void addPerson_createAndDeleteInGracePeriod() {
        //Add Fiona
        EditPersonDialogHandle addPersonDialog = personListPanel.clickNew();
        addPersonDialog.enterNewValues(td.fiona).clickOk();

        PersonCardHandle fionaCard = personListPanel.navigateToPerson(td.fiona);
        assertTrue(fionaCard.isShowingGracePeriod("Adding"));
        assertMatching(fionaCard, td.fiona);

        //Delete Fiona
        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        sleepForGracePeriod();
        assertFalse(personListPanel.contains(td.fiona));

        //Add George
        addPersonDialog = personListPanel.clickNew();
        addPersonDialog.enterNewValues(td.george).clickOk();
        PersonCardHandle georgeCard = personListPanel.navigateToPerson(td.george);
        assertTrue(georgeCard.isShowingGracePeriod("Adding"));
        sleepForGracePeriod();
        assertTrue(personListPanel.isListMatching(td.alice, td.benson, td.charlie, td.dan, td.elizabeth, td.george));
    }

    @Test
    public void addPerson_addTwoPersons() {

        EditPersonDialogHandle addPersonDialog = personListPanel.clickNew();
        addPersonDialog.enterNewValues(td.fiona).clickOk();
        PersonCardHandle fionaCard = personListPanel.navigateToPerson(td.fiona);
        assertTrue(fionaCard.isShowingGracePeriod("Adding"));
        assertMatching(fionaCard, td.fiona);

        addPersonDialog = personListPanel.clickNew();
        addPersonDialog.enterNewValues(td.george).clickOk();
        PersonCardHandle georgeCard = personListPanel.navigateToPerson(td.george);
        assertTrue(georgeCard.isShowingGracePeriod("Adding"));
        assertMatching(georgeCard, td.george);

        sleepForGracePeriod();
        assertTrue(personListPanel.isListMatching(td.alice, td.benson, td.charlie, td.dan, td.elizabeth, td.fiona,
                                                  td.george));
    }

    @Test
    public void addPerson_cancelDialog() {
        EditPersonDialogHandle addPersonDialog = personListPanel.clickNew();
        addPersonDialog.enterNewValues(td.fiona).clickCancel();
        assertTrue(personListPanel.isListMatching(td.getTestData()));
    }

    @Test
    public void addPerson_cancelAddOperationUsingAccelerator() {
        EditPersonDialogHandle addPersonDialog = personListPanel.clickNew();

        addPersonDialog.enterNewValues(td.fiona).clickOk();

        PersonCardHandle fionaCard = personListPanel.navigateToPerson(td.fiona);
        assertTrue(fionaCard.isShowingGracePeriod("Adding"));

        //Ensure correct state before cancelling.
        assertMatching(fionaCard, td.fiona);

        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();

        //Ensure cancel operation stops grace period display.
        assertFalse(fionaCard.isShowingGracePeriod("Adding"));

        //Confirm header status bar message
        assertEquals(HeaderStatusBarHandle.formatAddCancelledMessage(td.fiona.fullName(), Optional.empty()),
                     statusBar.getText());

        //Confirm the correctness of the entire list.
        assertTrue(personListPanel.isListMatching(td.getTestData()));
    }

    @Test
    public void addPerson_cancelAddOperationUsingContextMenu() {
        EditPersonDialogHandle addPersonDialog = personListPanel.clickNew();

        addPersonDialog.enterNewValues(td.george).clickOk();

        PersonCardHandle georgeCard = personListPanel.navigateToPerson(td.george);
        assertTrue(georgeCard.isShowingGracePeriod("Adding"));

        //Ensure correct state before cancelling.
        assertMatching(georgeCard, td.george);

        personListPanel.rightClickOnPerson(td.george).clickOnContextMenu(ContextMenuChoice.CANCEL);

        //Ensure cancel operation stops grace period display.
        assertFalse(georgeCard.isShowingGracePeriod("Adding"));

        //Confirm header status bar message
        assertEquals(HeaderStatusBarHandle.formatAddCancelledMessage(td.george.fullName(), Optional.empty()),
                     statusBar.getText());

        //Confirm the correctness of the entire list.
        assertTrue(personListPanel.isListMatching(td.getTestData()));
    }
}
