package guitests;

import address.model.datatypes.AddressBook;
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

        assertMatching(fionaCard, td.fiona);
        assertMatching(georgeCard, td.george);
    }

    @Test
    public void addPerson_cancelDialog() {
        EditPersonDialogHandle addPersonDialog = personListPanel.clickNew();
        addPersonDialog.enterNewValues(td.fiona).clickCancel();
        assertFalse(personListPanel.contains(td.fiona));
    }

    @Test
    public void addPerson_cancelAddOperation() {
        EditPersonDialogHandle addPersonDialog = personListPanel.clickNew();

        addPersonDialog.enterNewValues(td.fiona).clickOk();

        PersonCardHandle fionaCard = personListPanel.navigateToPerson(td.fiona);
        assertTrue(fionaCard.isShowingGracePeriod("Adding"));
        assertMatching(fionaCard, td.fiona); //Ensure correct state before cancelling.

        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertFalse(personListPanel.contains(td.fiona));
        assertFalse(fionaCard.isShowingGracePeriod("Adding"));
        assertEquals(HeaderStatusBarHandle.formatCancelledMessage(HeaderStatusBarHandle.Type.ADD, td.fiona.fullName(),
                     Optional.empty()), statusBar.getText());
    }
}
