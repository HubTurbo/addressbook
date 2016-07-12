package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.ReadOnlyAddressBook;
import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;
import address.sync.cloud.model.CloudAddressBook;
import address.testutil.PersonBuilder;
import address.util.TestUtil;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.PersonCardHandle;
import org.junit.Test;

import static address.util.TestUtil.descOnFail;
import static guitests.PersonEditGuiTest.TypicalData.*;
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

    private AddressBook initialData = generateInitialData();


    private AddressBook generateInitialData() {//TODO: create a better set of sample data
        AddressBook ab = new AddressBook();
        ab.addPerson(ALICE);
        ab.addPerson(BENSON);
        ab.addPerson(CHARLIE);
        ab.addPerson(DAN);
        ab.addPerson(ELIZABETH);
        ab.addTag(COLLEAGUES);
        ab.addTag(FRIENDS);
        return ab;
    }

    static class TypicalData{
        static Person ALICE = new Person("Alice", "Brown", 1);
        static Person BENSON = new Person("Benson", "Christopher Dean", 2);
        static Person CHARLIE = new Person("Charlie", "Davidson", 3);
        static Person DAN = new Person("Dan", "Edwards", 4);
        static Person ELIZABETH = new Person("Elizabeth", "F. Green", 5);
        static Tag COLLEAGUES = new Tag("colleagues");
        static Tag FRIENDS = new Tag("friends");
    }

    @Override
    protected AddressBook getInitialData() {
        return initialData;
    }

    @Test
    public void editPerson() {

        //Prepare new values for Alice
        Person newAlice = new PersonBuilder(ALICE.copy()).withFirstName("Alicia").withLastName("Brownstone")
                .withStreet("Updated street").withCity("Singapore").withPostalCode("123123")
                .withBirthday("01.01.1979").withGithubUsername("alicebrown123").withTags(COLLEAGUES, FRIENDS).build();

        //Get a reference to the card displaying Alice's details
        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(ALICE);

        //Edit Alice to change to new values
        personListPanel.clickOnPerson(ALICE);
        EditPersonDialogHandle editPersonDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        editPersonDialog.enterNewValues(newAlice);
        editPersonDialog.pressEnter();

        assertTrue(descOnFail(alicePersonCard, newAlice), alicePersonCard.isSamePerson(newAlice));
        //TODO: write a more elegant function that uses assertEquals but compare two different types of objects

        //TODO: Confirm the right card is selected after the edit, as follows
        //assertEquals(1, personListPanel.getSelectedCards().size();
        //assertEquals(alicePersonCard, personListPanel.getSelectedCards().get(0);

        //Confirm right values are displayed after grace period is over
        guiRobot.sleep(5000); //wait for grace period
        //TODO: link up to grace period
        //TODO: confirm the person card display behave as expected during grace period
        assertTrue(descOnFail(alicePersonCard, newAlice), alicePersonCard.isSamePerson(newAlice));

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
