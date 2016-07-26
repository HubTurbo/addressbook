package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import address.testutil.PersonBuilder;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.PersonCardHandle;
import guitests.guihandles.PersonListPanelHandle;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

/**
 * System tests for 'Edit person' feature.
 * <p> </p>NOT TESTED: <br>
 * * Tab order in the edit form: covered in GUI unit tests. <br>
 * * Tag search using partial words: covered in GUI unit tests. <br>
 * * Data validation. <br>
 */
public class PersonEditGuiTest extends GuiTestBase {

    @Override
    protected AddressBook getInitialData() {
        return td.book;
    }

    @Test
    public void editPerson_usingAccelerator() {

        //Prepare new values for Alice
        Person newAlice = new PersonBuilder(td.alice.copy()).withFirstName("Alicia").withLastName("Brownstone")
                .withStreet("Updated street").withCity("Singapore").withPostalCode("123123")
                .withBirthday("01.01.1979").withGithubUsername("alicebrown123").withTags(td.colleagues, td.friends).build();

        //Get a reference to the card displaying Alice's details
        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);

        //Edit Alice to change to new values
        personListPanel.clickOnPerson(td.alice);
        EditPersonDialogHandle editPersonDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        editPersonDialog.enterNewValues(newAlice).pressEnter();

        //Confirm pending state correctness
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));

        //Confirm the right card is selected after the edit
        assertEquals(alicePersonCard, newAlice);
        assertTrue(personListPanel.isSelected(newAlice));

        //Confirm right values are displayed after grace period is over
        sleepForGracePeriod();
        assertEquals(alicePersonCard, newAlice);

        //Confirm cancel operation does not cancel the edit after the grace period.
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        //Confirm the underlying person object has the right values
        assertEquals(alicePersonCard, newAlice);

        //Confirm again after the next sync
        sleep(getTestingConfig().getUpdateInterval(), TimeUnit.MILLISECONDS);
        assertEquals(newAlice.toString(), personListPanel.getSelectedPerson().toString());

        //Confirm other cards are unaffected.
        assertTrue(personListPanel.isListMatching(1, td.benson, td.charlie, td.dan, td.elizabeth));

        //Confirm status bar is updated correctly
        assertEquals(statusBar.getText(), "Edit Person [ Alice Brown -> Alicia Brownstone ] completed successfully.");
    }

    @Test
    public void editPerson_usingContextMenu() {
        personListPanel.rightClickOnPerson(td.alice);
        EditPersonDialogHandle editPersonDialog =
                personListPanel.clickOnContextMenu(PersonListPanelHandle.ContextMenuChoice.EDIT);
        assertTrue(editPersonDialog.isValidEditDialog());

        //Rest of the edit process tested in editPerson_usingAccelerator
    }

    @Test
    public void editPerson_usingEditButton() {
        personListPanel.clickOnPerson(td.alice);
        EditPersonDialogHandle editPersonDialog =  personListPanel.clickEdit();
        assertTrue(editPersonDialog.isValidEditDialog());

        //Rest of the edit process tested in editPerson_usingAccelerator
    }

    @Test
    public void editPerson_dataValidation() {

        personListPanel.clickOnPerson(td.alice);
        EditPersonDialogHandle editPersonDialog =  personListPanel.clickEdit();
        editPersonDialog.enterFirstName("Peter");
        editPersonDialog.enterLastName("");
        editPersonDialog.clickOk();

        assertTrue(editPersonDialog.isInputValidationErrorDialogShown());
        editPersonDialog.dissmissErrorMessage("Invalid Fields");
        assertFalse(editPersonDialog.isInputValidationErrorDialogShown());
    }

    @Test
    public void cancelOperation_usingAccelerator() {

        //Delete
        PersonCardHandle aliceCard = personListPanel.selectCard(td.alice);
        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        assertTrue(aliceCard.isShowingGracePeriod("Deleting"));

        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertFalse(aliceCard.isShowingGracePeriod("Deleting"));
        assertEquals(statusBar.getText(), "Delete Person [ " + aliceCard.getFirstName() + " "
                     + aliceCard.getLastName() + " ] was cancelled.");

        //Edit
        Person newAlice = new PersonBuilder(td.alice.copy()).withFirstName("Alicia").withLastName("Brownstone")
                .withStreet("Updated street").withCity("Singapore").withPostalCode("123123")
                .withBirthday("01.01.1979").withGithubUsername("alicebrown123").withTags(td.colleagues, td.friends).build();

        //Get a reference to the card displaying Alice's details
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);

        //Edit Alice to change to new values
        EditPersonDialogHandle editPersonDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        assertTrue(editPersonDialog.isShowingPerson(td.alice));
        editPersonDialog.enterNewValues(newAlice).pressEnter();
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));
        personListPanel.clickOnPerson(newAlice);
        assertEquals(alicePersonCard, newAlice);
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertEquals(alicePersonCard, td.alice);
        assertFalse(alicePersonCard.isShowingGracePeriod("Editing"));
        assertEquals(statusBar.getText(), "Edit Person [ Alice Brown -> Alicia Brownstone ] was cancelled.");

        //New
        EditPersonDialogHandle addPersonDialog = personListPanel.clickNew();
        Person pandaWong = new PersonBuilder("Panda", "Wong")
                .withStreet("Chengdu Panda Street").withCity("Chengdu").withPostalCode("PANDA")
                .withBirthday("01.01.1979").withGithubUsername("panda").withTags(td.colleagues, td.friends).build();
        addPersonDialog.enterNewValues(pandaWong).clickOk();

        personListPanel.navigateToPerson(pandaWong);
        PersonCardHandle pandaWongCardHandle = personListPanel.getPersonCardHandle(pandaWong);
        assertTrue(pandaWongCardHandle.isShowingGracePeriod("Adding"));
        assertEquals(pandaWongCardHandle, pandaWong); //Ensure correct state before cancelling.

        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertNull(personListPanel.getPersonCardHandle(pandaWong));
        assertFalse(pandaWongCardHandle.isShowingGracePeriod("Adding"));
        assertEquals("Add Person [ Panda Wong ] was cancelled.", statusBar.getText());
    }

    @Test
    public void editPerson_editDuringGracePeriod() {
        Person newAlice = new PersonBuilder(td.alice.copy()).withFirstName("Alicia").withLastName("Brownstone")
                .withStreet("Updated street").withCity("Singapore").withPostalCode("123123")
                .withBirthday("01.01.1979").withGithubUsername("alicebrown123").withTags(td.colleagues, td.friends).build();

        //Get a reference to the card displaying Alice's details
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);

        //Edit Alice to change to new values
        EditPersonDialogHandle editPersonDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        assertTrue(editPersonDialog.isShowingPerson(td.alice));
        editPersonDialog.enterNewValues(newAlice).pressEnter();

        //Ensure grace period is showing
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));

        //Edit Alice again during pending state.
        personListPanel.clickOnPerson(newAlice);
        Person newerAlice = new PersonBuilder(newAlice.copy()).withFirstName("Felicia").withLastName("Yellowstone")
                .withStreet("street updated").withCity("Malaysia").withPostalCode("321321")
                .withBirthday("11.11.1979").withGithubUsername("yellowstone").withTags(td.colleagues).build();

        editPersonDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        assertTrue(editPersonDialog.isShowingPerson(newAlice));

        //Ensure grace period is not counting down while editing person.
        assertTrue(alicePersonCard.isGracePeriodFrozen());

        editPersonDialog.enterNewValues(newerAlice).pressEnter();

        //Ensure card is displaying Felicia before and after grace period.
        assertEquals(alicePersonCard, newerAlice);
        sleepForGracePeriod();
        assertTrue(personListPanel.isListMatching(newerAlice, td.benson, td.charlie, td.dan, td.elizabeth));
    }

    @Test
    public void cancelOperation_afterGracePeriod() {
        //Tested in editPerson_usingAccelerator()
    }
}
