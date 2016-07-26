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
    public void editPerson_usingContextMenu() {

        //Prepare new values for Alice
        Person newAlice = new PersonBuilder(td.alice.copy()).withFirstName("Alicia").withLastName("Brownstone")
                .withStreet("Updated street").withCity("Singapore").withPostalCode("123123")
                .withBirthday("01.01.1979").withGithubUsername("alicebrown123").withTags(td.colleagues, td.friends).build();

        //Get a reference to the card displaying Alice's details
        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);

        personListPanel.rightClickOnPerson(td.alice);
        EditPersonDialogHandle editPersonDialog =
                personListPanel.clickOnContextMenu(PersonListPanelHandle.ContextMenuChoice.EDIT);
        assertTrue(editPersonDialog.isShowingPerson(td.alice));
        editPersonDialog.enterNewValues(newAlice).pressEnter();

        //Confirm pending state correctness
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));

        //Confirm the right card is selected after the edit
        assertMatching(alicePersonCard, newAlice);
        assertTrue(personListPanel.isSelected(newAlice));

        //Confirm right values are displayed after grace period is over
        sleepForGracePeriod();
        assertMatching(alicePersonCard, newAlice);

        //Confirm cancel operation does not cancel the edit after the grace period.
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        //Confirm the underlying person object has the right values
        assertMatching(alicePersonCard, newAlice);

        //Confirm again after the next sync
        sleep(getTestingConfig().getUpdateInterval(), TimeUnit.MILLISECONDS);
        assertEquals(newAlice.toString(), personListPanel.getSelectedPerson().toString());

        //Confirm other cards are unaffected.
        assertTrue(personListPanel.isListMatching(1, td.benson, td.charlie, td.dan, td.elizabeth));

        //Confirm status bar is updated correctly
        assertEquals(statusBar.getText(), "Edit Person [ Alice Brown -> Alicia Brownstone ] completed successfully.");

    }

    //TODO: This maybe should not be here. A separate class to test the button does what it suppose to do?
    @Test
    public void editPerson_usingEditButton() {
        personListPanel.clickOnPerson(td.alice);
        EditPersonDialogHandle editPersonDialog =  personListPanel.clickEdit();;
        assertTrue(editPersonDialog.isShowingPerson(td.alice));

        //Rest of the edit process tested in editPerson_usingContextMenu
    }

    @Test
    public void editPerson_dataValidation() {

        personListPanel.clickOnPerson(td.alice);
        EditPersonDialogHandle editPersonDialog =  personListPanel.clickEdit();
        assertTrue(editPersonDialog.isShowingPerson(td.alice));
        editPersonDialog.enterFirstName("Peter");
        editPersonDialog.enterLastName("");
        editPersonDialog.clickOk();

        assertTrue(editPersonDialog.isInputValidationErrorDialogShown());
        editPersonDialog.dissmissErrorMessage("Invalid Fields");
        assertFalse(editPersonDialog.isInputValidationErrorDialogShown());
    }

    @Test
    public void cancelOperation_usingAccelerator() {
        //Edit
        Person newAlice = new PersonBuilder(td.alice.copy()).withFirstName("Alicia").withLastName("Brownstone")
                .withStreet("Updated street").withCity("Singapore").withPostalCode("123123")
                .withBirthday("01.01.1979").withGithubUsername("alicebrown123").withTags(td.colleagues, td.friends).build();

        //Get a reference to the card displaying Alice's details
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);

        //Edit Alice to change to new values
        EditPersonDialogHandle editPersonDialog = personListPanel.editPerson(td.alice);
        editPersonDialog.enterNewValues(newAlice).pressEnter();
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));
        personListPanel.clickOnPerson(newAlice);
        assertMatching(alicePersonCard, newAlice);
        //assertEquals(alicePersonCard, newAlice);
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertMatching(alicePersonCard, td.alice);
        assertFalse(alicePersonCard.isShowingGracePeriod("Editing"));
        assertEquals(statusBar.getText(), "Edit Person [ Alice Brown -> Alicia Brownstone ] was cancelled.");
    }

    @Test
    public void editPerson_editDuringGracePeriod() {
        Person newAlice = new PersonBuilder(td.alice.copy()).withFirstName("Alicia").withLastName("Brownstone")
                .withStreet("Updated street").withCity("Singapore").withPostalCode("123123")
                .withBirthday("01.01.1979").withGithubUsername("alicebrown123").withTags(td.colleagues, td.friends).build();

        //Get a reference to the card displaying Alice's details
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);

        //Edit Alice to change to new values
        EditPersonDialogHandle editPersonDialog = personListPanel.editPerson(td.alice);
        editPersonDialog.enterNewValues(newAlice).pressEnter();

        //Ensure grace period is showing
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));

        //Edit Alice again during pending state.
        personListPanel.clickOnPerson(newAlice);
        Person newerAlice = new PersonBuilder(newAlice.copy()).withFirstName("Felicia").withLastName("Yellowstone")
                .withStreet("street updated").withCity("Malaysia").withPostalCode("321321")
                .withBirthday("11.11.1979").withGithubUsername("yellowstone").withTags(td.colleagues).build();

        editPersonDialog = personListPanel.editPerson(newAlice);

        //Get a reference to the current

        //Ensure grace period is not counting down while editing person.
        assertTrue(alicePersonCard.isGracePeriodFrozen());

        editPersonDialog.enterNewValues(newerAlice).pressEnter();

        //Ensure card is displaying Felicia before and after grace period.
        assertMatching(alicePersonCard, newerAlice);
        sleepForGracePeriod();
        assertTrue(personListPanel.isListMatching(newerAlice, td.benson, td.charlie, td.dan, td.elizabeth));
    }

    @Test
    public void cancelOperation_afterGracePeriod() {
        //Tested in editPerson_usingContextMenu()
    }
}
