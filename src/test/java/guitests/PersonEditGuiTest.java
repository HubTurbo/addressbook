package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import address.testutil.PersonBuilder;
import address.testutil.TestUtil;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.HeaderStatusBarHandle;
import guitests.guihandles.PersonCardHandle;
import org.junit.Test;

import java.util.Optional;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        return new AddressBook(td.book);
    }

    @Test
    public void editPerson_editAndDeleteDuringEditingGracePeriod() {
        PersonCardHandle elizabethCard = personListPanel.navigateToPerson(td.elizabeth);
        EditPersonDialogHandle editPersonDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        assertTrue(editPersonDialog.isShowingPerson(td.elizabeth));
        editPersonDialog.enterNewValues(elizabethEdited).clickOk();

        personListPanel.use_PERSON_DELETE_ACCELERATOR();

        assertTrue(personListPanel.isListMatching(TestUtil.removePersonsFromList(td.getTestData(), td.elizabeth)));
    }

    @Test
    public void editPerson_editDeleteAndEditDuringGracePeriod() {
        /* TODO: To include this test if future implementation allows this.
        PersonCardHandle elizabethCard = personListPanel.navigateToPerson(td.elizabeth);
        EditPersonDialogHandle editPersonDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        assertTrue(editPersonDialog.isShowingPerson(td.elizabeth));
        editPersonDialog.enterNewValues(elizabethEdited).clickOk();

        //Ensure pending state correctness
        assertTrue(elizabethCard.isShowingGracePeriod("Editing"));
        assertMatching(elizabethCard, elizabethEdited);

        //Delete during pending state
        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        assertTrue(elizabethCard.isShowingGracePeriod("Deleting"));

        //Edit again during deleting pending state
        editPersonDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        sleepForGracePeriod();
        assertTrue(editPersonDialog.isShowingPerson(elizabethEdited));
        editPersonDialog.enterNewValues(td.fiona).clickOk();


        assertTrue(elizabethCard.isShowingGracePeriod("Editing"));
        assertMatching(elizabethCard, td.fiona);

        sleepForGracePeriod();

        assertFalse(personListPanel.isAnyCardShowingGracePeriod());
        assertTrue(personListPanel.isListMatching(td.alice, td.benson, td.charlie, td.dan, td.fiona));
        */
    }

    @Test
    public void editPerson_usingContextMenu() {

        //Get a reference to the card displaying Alice's details
        PersonCardHandle aliceCard = personListPanel.getPersonCardHandle(td.alice);

        EditPersonDialogHandle editPersonDialog = personListPanel.rightClickOnPerson(td.alice)
                                                                 .clickOnContextMenuEdit();
        assertTrue(editPersonDialog.isShowingPerson(td.alice));

        editPersonDialog.enterNewValues(aliceEdited).pressEnter();

        assertMatching(aliceCard, aliceEdited);

        //Confirm the right card is selected after the edit
        assertTrue(personListPanel.isSelected(aliceEdited));

        assertMatching(aliceCard, aliceEdited);

        // Confirm other cards are unaffected.
        assertTrue(personListPanel.isListMatching(1, td.benson, td.charlie, td.dan, td.elizabeth));
    }

    @Test
    public void editPerson_usingEditButton() {
        personListPanel.navigateToPerson(td.elizabeth);
        EditPersonDialogHandle editPersonDialog =  personListPanel.clickEdit();
        assertTrue(editPersonDialog.isShowingPerson(td.elizabeth));
        editPersonDialog.enterNewValues(elizabethEdited).clickOk();
        assertTrue(personListPanel.isListMatching(TestUtil.replacePersonFromList(td.getTestData(), elizabethEdited, 4)));
        //Full edit process is done at editPerson_usingContextMenu()
    }

    @Test
    public void editPerson_usingAccelerator() {
        personListPanel.navigateToPerson(td.benson);
        EditPersonDialogHandle editPersonDialogHandle = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        assertTrue(editPersonDialogHandle.isShowingPerson(td.benson));
        editPersonDialogHandle.enterNewValues(bensonEdited);
        editPersonDialogHandle.clickOk();
        assertTrue(personListPanel.isListMatching(TestUtil.replacePersonFromList(td.getTestData(), bensonEdited, 1)));
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
        editPersonDialog.dismissErrorMessage("Invalid Fields");
        assertFalse(editPersonDialog.isInputValidationErrorDialogShown());

        //To test editPerson_cancelEditDialog()
        editPersonDialog.clickCancel();
        assertFalse(personListPanel.isAnyCardShowingGracePeriod());
        assertEquals(HeaderStatusBarHandle.formatEditCancelledMessage(td.elizabeth.fullName(), Optional.empty()),
                     statusBar.getText());
        assertTrue(personListPanel.isListMatching(td.getTestData()));
    }

    @Test
    public void editPerson_cancelEditDialog() {
        //Tested in editPerson_dataValidation()
    }

    @Test
    public void editPerson_cancelEditOperationUsingAccelerator() {
        //Get a reference to the card displaying Alice's details
        PersonCardHandle charlieCard = personListPanel.selectCard(td.charlie);

        //Edit Alice to change to new values
        EditPersonDialogHandle editPersonDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        assertTrue(editPersonDialog.isShowingPerson(td.charlie));
        editPersonDialog.enterNewValues(charlieEdited).pressEnter();

        //Confirm pending state correctness
        assertTrue(charlieCard.isShowingGracePeriod("Editing"));
        assertMatching(charlieCard, charlieEdited);

        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();

        //Ensure cancel operation remove display of grace period and show correct status bar message
        assertFalse(charlieCard.isShowingGracePeriod("Editing"));
        assertEquals(HeaderStatusBarHandle.formatEditCancelledMessage(td.charlie.fullName(),
                Optional.of(charlieEdited.fullName())),
                statusBar.getText());

        //Ensure the correctness of the entire list.
        assertTrue(personListPanel.isListMatching(td.getTestData()));
    }

    @Test
    public void editPerson_cancelEditOperationUsingContextMenu() {
        //Get a reference to the card displaying Alice's details
        PersonCardHandle danCard = personListPanel.navigateToPerson(td.dan);

        //Edit Alice to change to new values
        EditPersonDialogHandle editPersonDialog = personListPanel.rightClickOnPerson(td.dan)
                                                                 .clickOnContextMenuEdit();
        assertTrue(editPersonDialog.isShowingPerson(td.dan));
        editPersonDialog.enterNewValues(danEdited).pressEnter();

        //Confirm pending state correctness
        assertTrue(danCard.isShowingGracePeriod("Editing"));
        assertMatching(danCard, danEdited);

        personListPanel.rightClickOnPerson(danEdited).clickOnContextMenuCancel();

        //Ensure cancel operation remove display of grace period and show correct status bar message
        assertFalse(danCard.isShowingGracePeriod("Editing"));
        assertEquals(HeaderStatusBarHandle.formatEditCancelledMessage(td.dan.fullName(),
                                                                      Optional.of(danEdited.fullName())),
                     statusBar.getText());

        //Ensure the correctness of the entire list.
        assertTrue(personListPanel.isListMatching(td.getTestData()));
    }

    @Test
    public void editPerson_editDuringGracePeriod() {
        //Get a reference to the card displaying Alice's details
        PersonCardHandle aliceCard = personListPanel.selectCard(td.alice);

        // Edit Alice to change to new values
        EditPersonDialogHandle editPersonDialog = personListPanel.editPerson(td.alice);
        assertTrue(editPersonDialog.isShowingPerson(td.alice));
        editPersonDialog.enterNewValues(aliceEdited).pressEnter();

        //Edit Alice again
       editPersonDialog = personListPanel.editPerson(aliceEdited);

        //Ensure grace period is not counting down while editing person.
        Person newerAlice = new PersonBuilder(aliceEdited.copy()).withFirstName("Abba").withLastName("Yellowstone")
                .withStreet("street updated").withCity("Malaysia").withPostalCode("321321")
                .withBirthday("11.11.1979").withGithubUsername("yellowstone").withTags(td.colleagues).build();
        editPersonDialog.enterNewValues(newerAlice).pressEnter();

        //Verify the other cards are not affected.
        assertTrue(personListPanel.isListMatching(1, td.benson, td.charlie, td.dan, td.elizabeth));
    }

    @Test
    public void editPerson_cancelOperationAfterGracePeriod() {
        //Tested in editPerson_usingContextMenu()
    }
    //TODO: testing edits during grace period
}
