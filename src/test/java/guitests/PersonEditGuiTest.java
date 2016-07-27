package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import address.testutil.PersonBuilder;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.HeaderStatusBarHandle;
import guitests.guihandles.PersonCardHandle;
import guitests.guihandles.PersonListPanelHandle;
import org.junit.Test;

import java.util.Optional;
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

        //Get a reference to the card displaying Alice's details
        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);

        personListPanel.rightClickOnPerson(td.alice);
        EditPersonDialogHandle editPersonDialog =
                personListPanel.clickOnContextMenu(PersonListPanelHandle.ContextMenuChoice.EDIT);
        assertTrue(editPersonDialog.isShowingPerson(td.alice));
        //Prepare new values for Alice
        Person newAlice = new PersonBuilder(td.alice.copy()).withFirstName("Alicia").withLastName("Brownstone")
                .withStreet("Updated street").withCity("Singapore").withPostalCode("123123")
                .withBirthday("01.01.1979").withGithubUsername("alicebrown123").withTags(td.colleagues, td.friends).build();
        editPersonDialog.enterNewValues(newAlice).pressEnter();
        assertMatching(alicePersonCard, newAlice);

        //Confirm pending state correctness
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));

        //Confirm the right card is selected after the edit
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
        assertEquals(HeaderStatusBarHandle.formatSuccessMessage(HeaderStatusBarHandle.Type.EDIT, td.alice.fullName(),
                     Optional.of(newAlice.fullName())), statusBar.getText());
    }

    @Test
    public void editPerson_usingEditButton() {
        personListPanel.navigateToPerson(td.elizabeth);
        EditPersonDialogHandle editPersonDialog =  personListPanel.clickEdit();
        assertTrue(editPersonDialog.isShowingPerson(td.elizabeth));
        editPersonDialog.enterNewValues(td.elizabethEdited).clickOk();
        sleepForGracePeriod();
        assertTrue(personListPanel.contains(td.elizabethEdited));
        assertFalse(personListPanel.contains(td.elizabeth));
        //Full edit process is done at editPerson_usingContextMenu()
    }

    @Test
    public void editPerson_usingAccelerator() {
        personListPanel.navigateToPerson(td.benson);
        EditPersonDialogHandle editPersonDialogHandle = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        assertTrue(editPersonDialogHandle.isShowingPerson(td.benson));
        editPersonDialogHandle.enterNewValues(td.bensonEdited);
        editPersonDialogHandle.clickOk();
        sleepForGracePeriod();
        assertTrue(personListPanel.contains(td.bensonEdited));
        assertFalse(personListPanel.contains(td.benson));
        //Full edit process is done at editPerson_usingContextMenu()
    }

    @Test
    public void editPerson_dataValidation() {

        personListPanel.clickOnPerson(td.elizabeth);
        EditPersonDialogHandle editPersonDialog =  personListPanel.clickEdit();
        assertTrue(editPersonDialog.isShowingPerson(td.elizabeth));
        editPersonDialog.enterFirstName("Peter");
        editPersonDialog.enterLastName("");
        editPersonDialog.clickOk();

        assertTrue(editPersonDialog.isInputValidationErrorDialogShown());
        editPersonDialog.dissmissErrorMessage("Invalid Fields");
        assertFalse(editPersonDialog.isInputValidationErrorDialogShown());
    }

    @Test
    public void cancelOperation_usingAccelerator() {
        //Get a reference to the card displaying Alice's details
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.charlie);

        //Edit Alice to change to new values
        EditPersonDialogHandle editPersonDialog = personListPanel.editPerson(td.charlie);
        Person newCharlie = new PersonBuilder(td.charlie.copy()).withFirstName("Charlotte").withLastName("Talon")
                .withStreet("Updated street").withCity("Singapore").withPostalCode("123123")
                .withBirthday("01.01.1979").withGithubUsername("charlotte").withTags(td.colleagues, td.friends).build();
        editPersonDialog.enterNewValues(newCharlie).pressEnter();
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));
        assertMatching(alicePersonCard, newCharlie);
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertMatching(alicePersonCard, td.charlie);
        assertFalse(alicePersonCard.isShowingGracePeriod("Editing"));
        assertEquals(HeaderStatusBarHandle.formatCancelledMessage(HeaderStatusBarHandle.Type.EDIT, td.charlie.fullName(),
                     Optional.of(newCharlie.fullName())), statusBar.getText());
    }

    @Test
    public void editPerson_editDuringGracePeriod() {
        //Get a reference to the card displaying Alice's details
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);

        //Edit Alice to change to new values
        EditPersonDialogHandle editPersonDialog = personListPanel.editPerson(td.alice);
        Person newAlice = new PersonBuilder(td.alice.copy()).withFirstName("Alicia").withLastName("Brownstone")
                .withStreet("Updated street").withCity("Singapore").withPostalCode("123123")
                .withBirthday("01.01.1979").withGithubUsername("alicebrown123").withTags(td.colleagues, td.friends).build();
        editPersonDialog.enterNewValues(newAlice).pressEnter();

        //Ensure grace period is showing
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));

        //Edit Alice again during pending state.
        Person newerAlice = new PersonBuilder(newAlice.copy()).withFirstName("Abba").withLastName("Yellowstone")
                .withStreet("street updated").withCity("Malaysia").withPostalCode("321321")
                .withBirthday("11.11.1979").withGithubUsername("yellowstone").withTags(td.colleagues).build();
        editPersonDialog = personListPanel.editPerson(newAlice);

        //Ensure grace period is not counting down while editing person.
        assertTrue(alicePersonCard.isGracePeriodFrozen());

        editPersonDialog.enterNewValues(newerAlice).pressEnter();
        //TODO: Verify that the countdown is restarted.

        //Ensure card is displaying Abba before and after grace period.
        assertMatching(alicePersonCard, newerAlice);
        sleepForGracePeriod();
        assertTrue(personListPanel.isListMatching(newerAlice, td.benson, td.charlie, td.dan, td.elizabeth));
    }

    @Test
    public void cancelOperation_afterGracePeriod() {
        //Tested in editPerson_usingContextMenu()
    }
}
