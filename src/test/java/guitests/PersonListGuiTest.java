package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.ReadOnlyAddressBook;
import address.model.datatypes.person.Person;
import address.sync.cloud.model.CloudAddressBook;
import address.util.TestUtil;
import org.junit.Test;
import static guitests.PersonListGuiTest.TypicalData.*;

import static org.junit.Assert.assertTrue;

public class PersonListGuiTest extends GuiTestBase {

    private AddressBook initialData = generateInitialData();


    private AddressBook generateInitialData() {//TODO: create a better set of sample data
        AddressBook ab = new AddressBook();
        ab.addPerson(ALICE);
        ab.addPerson(BENSON);
        ab.addPerson(CHARLIE);
        ab.addPerson(DAN);
        ab.addPerson(ELIZABETH);
        return ab;
    }

    static class TypicalData{
        static Person ALICE = new Person("Alice", "Brown", 1);
        static Person BENSON = new Person("Benson", "Christopher Dean", 2);
        static Person CHARLIE = new Person("Charlie", "Davidson", 3);
        static Person DAN = new Person("Dan", "Edwards", 4);
        static Person ELIZABETH = new Person("Elizabeth", "F. Green", 5);
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
    public void dragAndDrop_singlePersonCorrectDrag_listReordered() {

        assertTrue(personListPanel.containsInOrder(ALICE, BENSON, CHARLIE, DAN, ELIZABETH));

        // drag first person (Alice) and drop on Charles
        personListPanel.dragAndDrop(ALICE.getFirstName(), CHARLIE.getFirstName());
        assertTrue(personListPanel.containsInOrder(BENSON, ALICE, CHARLIE, DAN, ELIZABETH));

        // drag a card (Dan) to the top
        personListPanel.dragAndDrop(DAN.getFirstName(), BENSON.getFirstName());
        assertTrue(personListPanel.containsInOrder(DAN, BENSON, ALICE, CHARLIE, ELIZABETH));

        //drag the person at the bottom and drop at the middle
        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(5); //Make the target card visible
        personListPanel.dragAndDrop(ELIZABETH.getFirstName(), CHARLIE.getFirstName());
        assertTrue(personListPanel.containsInOrder(DAN, BENSON, ALICE, ELIZABETH, CHARLIE));

        //drag the person at the middle and drop at the bottom
        //TODO: implement this
    }

    @Test
    public void dragAndDrop_singlePersonWrongDrag_listUnchanged() {
        assertTrue(personListPanel.containsInOrder(ALICE, BENSON, CHARLIE, DAN, ELIZABETH));

        personListPanel.dragAndDrop(CHARLIE.getFirstName(), CHARLIE.getFirstName());
        assertTrue(personListPanel.containsInOrder(ALICE, BENSON, CHARLIE, DAN, ELIZABETH));

        //TODO: test for Dropping outside list
    }

    @Test
    public void dragAndDrop_multiplePersonCorrectDrag_listReordered() {
        //TODO: implement this
    }

    @Test
    public void dragAndDrop_multiplePersonWrongDrag_listUnchanged() {
        //TODO: implement this
    }

}
