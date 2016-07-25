package guitests;

import address.model.ModelManager;
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
        editPersonDialog.enterNewValues(newAlice);
        editPersonDialog.pressEnter();

        mainGui.sleep(1, TimeUnit.SECONDS);

        //Confirm pending state correctness
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));

        //Confirm the right card is selected after the edit
        assertEquals(alicePersonCard, newAlice);
        assertTrue(personListPanel.isSelected(newAlice));


        //Confirm right values are displayed after grace period is over
        mainGui.sleepForGracePeriod();
        assertEquals(alicePersonCard, newAlice);

        //Confirm the underlying person object has the right values
        assertEquals(newAlice.toString(), personListPanel.getSelectedPerson().toString());


        //Confirm again after the next sync
        mainGui.sleep(getTestingConfig().getUpdateInterval(), TimeUnit.MILLISECONDS);
        assertEquals(newAlice.toString(), personListPanel.getSelectedPerson().toString());

        //Confirm other cards are unaffected.
        personListPanel.clickOnListView();
        assertTrue(personListPanel.isListMatching(1, td.benson, td.charlie, td.dan, td.elizabeth));

        //Confirm status bar is updated correctly
        assertEquals(statusBar.getText(), "Edit Person [ Alice Brown -> Alicia Brownstone ] completed successfully.");
    }


    @Test
    public void editPerson_usingContextMenu() {
        personListPanel.rightClickOnPerson(td.alice);
        EditPersonDialogHandle editPersonDialog = personListPanel.clickOnContextMenu(
                PersonListPanelHandle.ContextMenuChoice.EDIT);
        assertTrue(editPersonDialog.isValidEditDialog());
    }


    @Test
    public void editPerson_usingEditButton() {
        personListPanel.clickOnPerson(td.alice);
        EditPersonDialogHandle editPersonDialog =  personListPanel.clickEdit();
        assertTrue(editPersonDialog.isValidEditDialog());
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
        mainGui.sleep(1, TimeUnit.SECONDS);
        assertTrue(aliceCard.isShowingGracePeriod("Deleting"));

        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        mainGui.sleep(1, TimeUnit.SECONDS);
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
        editPersonDialog.enterNewValues(newAlice);
        editPersonDialog.pressEnter();

        mainGui.focusOnMainApp();
        personListPanel.clickOnPerson(newAlice);

        assertNotEquals(alicePersonCard, td.alice);
        assertEquals(alicePersonCard, newAlice);
        mainGui.sleep(ModelManager.GRACE_PERIOD_DURATION/2, TimeUnit.SECONDS);
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertEquals(alicePersonCard, td.alice);
        assertEquals(statusBar.getText(), "Edit Person [ Alice Brown -> Alicia Brownstone ] was cancelled.");

        //New
        EditPersonDialogHandle addPersonDialog = personListPanel.clickNew();
        Person pandaWong = new PersonBuilder("Panda", "Wong")
                .withStreet("Chengdu Panda Street").withCity("Chengdu").withPostalCode("PANDA")
                .withBirthday("01.01.1979").withGithubUsername("panda").withTags(td.colleagues, td.friends).build();
        addPersonDialog.enterNewValues(pandaWong);
        addPersonDialog.clickOk();

        mainGui.focusOnMainApp();
        personListPanel.clickOnListView();
        personListPanel.use_LIST_GOTO_BOTTOM_SEQUENCE();
        personListPanel.clickOnPerson(pandaWong);
        PersonCardHandle pandaWongCardHandle = personListPanel.getSelectedCards().get(0);
        assertNotNull(pandaWongCardHandle);
        assertEquals(pandaWongCardHandle, pandaWong);
        mainGui.sleep(1, TimeUnit.SECONDS);
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertNull(personListPanel.getPersonCardHandle(pandaWong));
    }

    //TODO: testing edits during grace period
}
