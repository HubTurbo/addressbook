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
        assertTrue(personListPanel.isListMatching(td.getTestData()));
    }

    @Test
    public void editPerson_cancelEditDialog() {
        //Tested in editPerson_dataValidation()
    }
}
