package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import address.testutil.ContextMenuChoice;
import address.testutil.PersonBuilder;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.HeaderStatusBarHandle;
import guitests.guihandles.PersonCardHandle;
import org.junit.Test;

import java.util.Optional;

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

    public Person aliceEdited = new PersonBuilder(td.alice.copy()).withFirstName("Alicia").withLastName("Brownstone")
                                                                  .withStreet("81th Wall Street").withCity("New York")
                                                                  .withPostalCode("41452").withBirthday("11.09.1983")
                                                                  .withGithubUsername("alicia").withTags(td.friends).build();
    public Person bensonEdited = new PersonBuilder(td.benson.copy()).withFirstName("Ben").withLastName("Chris")
                                                                    .withStreet("Pittsburgh Square").withCity("Pittsburg")
                                                                    .withPostalCode("42445").withGithubUsername("ben").build();
    public Person charlieEdited = new PersonBuilder(td.charlie.copy()).withFirstName("Charlotte").withCity("Texas")
                                                                      .withGithubUsername("charlotte").build();
    public Person danEdited = new PersonBuilder(td.dan.copy()).withLastName("Edmonton").withBirthday("03.01.1995").build();
    public Person elizabethEdited = new PersonBuilder(td.elizabeth.copy()).withLastName("Green").build();


    @Override
    protected AddressBook getInitialData() {
        return td.book;
    }

    @Test
    public void editPerson_usingContextMenu() {

        //Get a reference to the card displaying Alice's details
        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);

        EditPersonDialogHandle editPersonDialog = personListPanel.rightClickOnPerson(td.alice)
                                                                 .clickOnContextMenu(ContextMenuChoice.EDIT);
        assertTrue(editPersonDialog.isShowingPerson(td.alice));
        editPersonDialog.enterNewValues(aliceEdited).pressEnter();

        //Confirm pending state correctness
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));
        assertMatching(alicePersonCard, aliceEdited);

        //Confirm the right card is selected after the edit
        assertTrue(personListPanel.isSelected(aliceEdited));

        //Confirm right values are displayed after grace period is over
        sleepForGracePeriod();
        assertMatching(alicePersonCard, aliceEdited);

        //Confirm cancel operation does not cancel the edit after the grace period.
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        //Confirm the underlying person object has the right values
        assertMatching(alicePersonCard, aliceEdited);

        //Confirm again after the next sync
        sleepUntilNextSync();
        assertEquals(aliceEdited.toString(), personListPanel.getSelectedPerson().toString());

        //Confirm other cards are unaffected.
        assertTrue(personListPanel.isListMatching(1, td.benson, td.charlie, td.dan, td.elizabeth));

        //Confirm status bar is updated correctly
        assertEquals(HeaderStatusBarHandle.formatEditSuccessMessage(td.alice.fullName(), Optional.of(aliceEdited.fullName())),
                     statusBar.getText());
    }

    @Test
    public void editPerson_usingEditButton() {
        personListPanel.navigateToPerson(td.elizabeth);
        EditPersonDialogHandle editPersonDialog =  personListPanel.clickEdit();
        assertTrue(editPersonDialog.isShowingPerson(td.elizabeth));
        editPersonDialog.enterNewValues(elizabethEdited).clickOk();
        sleepForGracePeriod();
        assertTrue(personListPanel.isListMatching(td.alice, td.benson, td.charlie, td.dan, elizabethEdited));
        //Full edit process is done at editPerson_usingContextMenu()
    }

    @Test
    public void editPerson_usingAccelerator() {
        personListPanel.navigateToPerson(td.benson);
        EditPersonDialogHandle editPersonDialogHandle = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        assertTrue(editPersonDialogHandle.isShowingPerson(td.benson));
        editPersonDialogHandle.enterNewValues(bensonEdited);
        editPersonDialogHandle.clickOk();
        sleepForGracePeriod();
        assertTrue(personListPanel.isListMatching(td.alice, bensonEdited, td.charlie, td.dan, td.elizabeth));
        //Full edit process is done at editPerson_usingContextMenu()
    }

    @Test
    public void editPerson_dataValidation() {
        personListPanel.navigateToPerson(td.elizabeth);
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
    public void editPerson_cancelPersonUsingAccelerator() {
        //Get a reference to the card displaying Alice's details
        PersonCardHandle charlieCard = personListPanel.selectCard(td.charlie);

        //Edit Alice to change to new values
        EditPersonDialogHandle editPersonDialog = personListPanel.editPerson(td.charlie);
        editPersonDialog.enterNewValues(charlieEdited).pressEnter();
        assertTrue(charlieCard.isShowingGracePeriod("Editing"));
        assertMatching(charlieCard, charlieEdited);
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertTrue(personListPanel.isListMatching(td.getTestData()));
        assertFalse(charlieCard.isShowingGracePeriod("Editing"));
        assertEquals(HeaderStatusBarHandle.formatEditCancelledMessage(td.charlie.fullName(),
                Optional.of(charlieEdited.fullName())),
                statusBar.getText());
    }

    @Test
    public void editPerson_cancelPersonUsingContextMenu() {
        //Get a reference to the card displaying Alice's details
        PersonCardHandle danCard = personListPanel.selectCard(td.dan);

        //Edit Alice to change to new values
        EditPersonDialogHandle editPersonDialog = personListPanel.editPerson(td.dan);
        editPersonDialog.enterNewValues(danEdited).pressEnter();
        assertTrue(danCard.isShowingGracePeriod("Editing"));
        assertMatching(danCard, charlieEdited);
        personListPanel.rightClickOnPerson(danEdited).clickOnContextMenu(ContextMenuChoice.CANCEL);
        assertTrue(personListPanel.isListMatching(td.getTestData()));
        assertFalse(danCard.isShowingGracePeriod("Editing"));
        assertEquals(HeaderStatusBarHandle.formatEditCancelledMessage(td.charlie.fullName(),
                Optional.of(charlieEdited.fullName())),
                statusBar.getText());
    }

    @Test
    public void editPerson_editDuringGracePeriod() {
        //Get a reference to the card displaying Alice's details
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);

        //Edit Alice to change to new values
        EditPersonDialogHandle editPersonDialog = personListPanel.editPerson(td.alice);
        editPersonDialog.enterNewValues(aliceEdited).pressEnter();

        //Ensure grace period is showing
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));

        //Edit Alice again during pending state.
       editPersonDialog = personListPanel.editPerson(aliceEdited);

        //Ensure grace period is not counting down while editing person.
        assertTrue(alicePersonCard.isGracePeriodFrozen());
        Person newerAlice = new PersonBuilder(aliceEdited.copy()).withFirstName("Abba").withLastName("Yellowstone")
                .withStreet("street updated").withCity("Malaysia").withPostalCode("321321")
                .withBirthday("11.11.1979").withGithubUsername("yellowstone").withTags(td.colleagues).build();
        editPersonDialog.enterNewValues(newerAlice).pressEnter();
        //TODO: Verify that the countdown is restarted.

        //Ensure card is displaying Abba before and after grace period.
        assertMatching(alicePersonCard, newerAlice);
        sleepForGracePeriod();
        assertMatching(alicePersonCard, newerAlice);

        //Verify the other cards are not affected.
        assertTrue(personListPanel.isListMatching(1, td.benson, td.charlie, td.dan, td.elizabeth));
    }

    @Test
    public void editPerson_cancelOperationAfterGracePeriod() {
        //Tested in editPerson_usingContextMenu()
    }
}
