package guitests;

import address.model.ModelManager;
import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import address.testutil.PersonBuilder;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.PersonCardHandle;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

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

    @Override
    protected AddressBook getInitialData() {
        return td.book;
    }

    @Test
    public void editPerson() {

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

        assertEquals(alicePersonCard, newAlice);
        //TODO: Confirm the right card is selected after the edit, as follows
        //assertEquals(1, personListPanel.getSelectedCards().size();
        //assertEquals(alicePersonCard, personListPanel.getSelectedCards().get(0);
        assertTrue(alicePersonCard.isPendingStateCountDownVisible());
        assertTrue(alicePersonCard.isPendingStateLabelVisible());
        assertFalse(alicePersonCard.isPendingStateProgressIndicatorVisible());
        assertTrue(alicePersonCard.getPendingStateLabel().equals("Edited"));

        //Confirm right values are displayed after grace period is over
        guiRobot.sleep(ModelManager.GRACE_PERIOD_DURATION + 1, TimeUnit.SECONDS); //wait for grace period
        assertEquals(alicePersonCard, newAlice);

        //Confirm the underlying person object has the right values
        assertEquals(newAlice.toString(), personListPanel.getSelectedPerson().toString());

        //TODO: confirm again after the next sync
        //TODO: confirm other cards are unaffected
        //TODO: confirm the right card is selected after the edit
        //TODO: confirm status bar is updated correctly

    }

    /* TODO:
     * Test using 'Edit' context menu
     * Test using 'Edit' button
     * Test 'Cancel'
     * Test 'OK'
     * Test data validation (just one case)
     */

}
