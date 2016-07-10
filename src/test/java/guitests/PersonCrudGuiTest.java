package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.ReadOnlyAddressBook;
import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;
import address.sync.cloud.model.CloudAddressBook;
import address.util.DateTimeUtil;
import address.util.TestUtil;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.PersonCardHandle;
import org.junit.Test;

import java.util.Arrays;

import static address.util.TestUtil.descOnFail;
import static guitests.PersonCrudGuiTest.TypicalData.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PersonCrudGuiTest extends GuiTestBase {

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
    protected ReadOnlyAddressBook getInitialData() {
        return initialData;
    }

    @Override
    protected CloudAddressBook getInitialCloudData() {
        return TestUtil.generateCloudAddressBook(initialData);
    }

    @Test
    public void editPerson() {

        //Prepare new values for Alice
        Person newAlice = ALICE.copy();
        newAlice.setFirstName("Alicia");
        newAlice.setLastName("Brownstone");
        newAlice.setStreet("Updated street");
        newAlice.setCity("Singapore");
        newAlice.setPostalCode("123123");
        newAlice.setBirthday(DateTimeUtil.parse("01.01.1979"));
        newAlice.setGithubUsername("alicebrown123");
        newAlice.setTags(Arrays.asList(new Tag[]{COLLEAGUES, FRIENDS}));

        //Edit new values for Alice
        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(ALICE);
        personListPanel.clickOnPerson(ALICE);
        EditPersonDialogHandle editPersonDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        editPersonDialog.enterNewValues(newAlice);
        editPersonDialog.pressEnter();

        assertTrue(descOnFail(alicePersonCard, newAlice), alicePersonCard.isSamePerson(newAlice));
        assertEquals(newAlice.getLastName(), alicePersonCard.getLastName());
        guiRobot.sleep(5000);
        assertTrue(descOnFail(alicePersonCard, newAlice), alicePersonCard.isSamePerson(newAlice));
        assertEquals(newAlice.toString(), personListPanel.getSelectedPerson().toString());

    }


}
