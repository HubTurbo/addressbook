package guitests;

import address.model.ModelManager;
import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import address.testutil.PersonBuilder;
import address.testutil.TestUtil;
import com.google.common.io.Files;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.PersonCardHandle;
import guitests.guihandles.PersonListPanelHandle;
import org.junit.Assert;
import org.junit.Test;
import org.loadui.testfx.GuiTest;

import java.io.File;
import java.io.IOException;
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
    public void editPerson_usingAccelerator() throws IOException {
/*
        //Prepare new values for Alice
        Person newAlice = new PersonBuilder(td.alice.copy()).withFirstName("Alicia").withLastName("Brownstone")
                .withStreet("Updated street").withCity("Singapore").withPostalCode("123123")
                .withBirthday("01.01.1979").withGithubUsername("alicebrown123").withTags(td.colleagues, td.friends).build();

        //Get a reference to the card displaying Alice's details
        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);

        if (alicePersonCard == null) {
            System.out.println("alicePersonCard is null");
        }

        //Edit Alice to change to new values
        personListPanel.clickOnPerson(td.alice);
        EditPersonDialogHandle editPersonDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        editPersonDialog.enterNewValues(newAlice);
        editPersonDialog.pressEnter();

        personListPanel.sleep(1, TimeUnit.SECONDS);

        //Confirm pending state correctness
        assertTrue(alicePersonCard.isPendingStateCountDownVisible());
        assertTrue(alicePersonCard.isPendingStateLabelVisible());
        assertFalse(alicePersonCard.isPendingStateProgressIndicatorVisible());
        assertTrue(alicePersonCard.getPendingStateLabel().equals("Edited"));

        //Confirm the right card is selected after the edit
        assertEquals(alicePersonCard, newAlice);
        assertEquals(1, personListPanel.getSelectedCards().size());
        assertEquals(alicePersonCard, personListPanel.getSelectedCards().get(0));


        //Confirm right values are displayed after grace period is over
        personListPanel.sleepForGracePeriod();
        assertEquals(alicePersonCard, newAlice);

        //Confirm the underlying person object has the right values
        assertEquals(newAlice.toString(), personListPanel.getSelectedPerson().toString());

        personListPanel.focusOnMainApp();
        personListPanel.enterFilterAndApply("haha");


        //Confirm again after the next sync
        personListPanel.sleep(getTestingConfig().getUpdateInterval(), TimeUnit.MILLISECONDS);
        assertEquals(newAlice.toString(), personListPanel.getSelectedPerson().toString());
*/
        //Confirm other cards are unaffected.

        //personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(1);
        //assertTrue(personListPanel.isSelected(td.alice));
        //personListPanel.clickOnPerson(td.alice);
        personListPanel.focusListView();
        File file;
        file = GuiTest.captureScreenshot();
        Files.copy(file, new File("key1.png"));
        //personListPanel.clearSelection();
        //personListPanel.focusOnMainApp();
        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(2);
        personListPanel.sleep(1, TimeUnit.SECONDS);
        file = GuiTest.captureScreenshot();
        Files.copy(file, new File("key2.png"));
        System.out.println("Selected Person: " + personListPanel.getSelectedPerson().toString());

        //assertTrue(personListPanel.isSelected(td.benson));
        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(3);
        personListPanel.sleep(1, TimeUnit.SECONDS);
        file = GuiTest.captureScreenshot();
        Files.copy(file, new File("key3.png"));
        System.out.println("Selected Person: " + personListPanel.getSelectedPerson().toString());
        //assertTrue(personListPanel.isSelected(td.charlie));
        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(4);
        personListPanel.sleep(1, TimeUnit.SECONDS);
        file = GuiTest.captureScreenshot();
        Files.copy(file, new File("key4.png"));
        System.out.println("Selected Person: " + personListPanel.getSelectedPerson().toString());
        //assertTrue(personListPanel.isSelected(td.dan));
        fail();

        //Confirm status bar is updated correctly
        //assertEquals(statusBar.getText(), "Alice Brown (old) -> Alicia Brownstone (new) has been edited successfully.");
    }


    @Test
    public void editPerson_usingContextMenu() {

        if (TestUtil.isHeadlessEnvironment()) {
            return;
        }

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
        Assert.assertFalse(editPersonDialog.isInputValidationErrorDialogShown());
        
    }

    @Test
    public void cancelPerson_usingAccelerator() throws IOException {

        //Delete
        personListPanel.use_LIST_GOTO_TOP_SEQUENCE();
        PersonCardHandle deletedCard = personListPanel.getPersonCardHandle(new Person(personListPanel.getSelectedPerson()));
        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        personListPanel.sleep(1, TimeUnit.SECONDS);
        //assertTrue(deletedCard.isPendingStateCountDownVisible());
       // assertTrue(deletedCard.isPendingStateLabelVisible());
        //assertFalse(deletedCard.isPendingStateProgressIndicatorVisible());
        //assertTrue(deletedCard.getPendingStateLabel().equals("Deleted"));
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        personListPanel.sleep(1, TimeUnit.SECONDS);
        //assertFalse(deletedCard.isPendingStateCountDownVisible());
        //assertFalse(deletedCard.isPendingStateProgressIndicatorVisible());
        //assertFalse(deletedCard.getPendingStateLabel().equals("Deleted"));
        assertEquals(statusBar.getText(), "Delete operation on " + deletedCard.getFirstName() + " "
                                          + deletedCard.getLastName() + " has been cancelled.");


        //Edit
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
        assertNotEquals(alicePersonCard, td.alice);
        assertEquals(alicePersonCard, newAlice);
        personListPanel.sleep(ModelManager.GRACE_PERIOD_DURATION/2, TimeUnit.SECONDS);
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertEquals(alicePersonCard, td.alice);
        assertEquals(statusBar.getText(), "Edit operation on " + alicePersonCard.getFirstName() + " "
                                            + alicePersonCard.getLastName() + " has been cancelled.");

        //New
        EditPersonDialogHandle addPersonDialog = personListPanel.clickNew();
        Person pandaWong = new PersonBuilder(td.alice.copy()).withFirstName("Panda").withLastName("Wong")
                .withStreet("Chengdu Panda Street").withCity("Chengdu").withPostalCode("PANDA")
                .withBirthday("01.01.1979").withGithubUsername("panda").withTags(td.colleagues, td.friends).build();
        addPersonDialog.enterNewValues(pandaWong);
        addPersonDialog.clickOk();

        personListPanel.use_LIST_GOTO_BOTTOM_SEQUENCE();
        PersonCardHandle pandaWongCardHandle = personListPanel.getSelectedCards().get(0);
        assertNotNull(pandaWongCardHandle);
        assertEquals(pandaWongCardHandle, pandaWong);
        pandaWongCardHandle.sleep(ModelManager.GRACE_PERIOD_DURATION/2, TimeUnit.SECONDS);
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        pandaWongCardHandle.sleep(1, TimeUnit.SECONDS);
        assertNull(personListPanel.getPersonCardHandle(pandaWong));

    }

}
