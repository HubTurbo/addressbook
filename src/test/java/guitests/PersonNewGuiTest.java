package guitests;

import address.model.datatypes.AddressBook;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.PersonCardHandle;
import org.junit.Test;


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
    public void addPerson_createAndDelete() {
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
        assertFalse(personListPanel.contains(td.fiona)); //Make sure Person A doesn't appear again.
        assertTrue(personListPanel.contains(td.george));
    }

    @Test
    public void addPerson_multipleAdd() {

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
    public void cancelOperation() {
        EditPersonDialogHandle addPersonDialog = personListPanel.clickNew();

        addPersonDialog.enterNewValues(td.fiona).clickOk();

        PersonCardHandle fionaCard = personListPanel.navigateToPerson(td.fiona);
        assertTrue(fionaCard.isShowingGracePeriod("Adding"));
        assertMatching(fionaCard, td.fiona); //Ensure correct state before cancelling.

        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertFalse(personListPanel.contains(td.fiona));
        assertFalse(fionaCard.isShowingGracePeriod("Adding"));
        assertEquals("Add Person [ Fiona Wong ] was cancelled.", statusBar.getText());
    }
}
