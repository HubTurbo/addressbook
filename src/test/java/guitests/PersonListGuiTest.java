package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.ReadOnlyAddressBook;
import address.model.datatypes.person.Person;
import address.sync.cloud.model.CloudAddressBook;
import address.util.TestUtil;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PersonListGuiTest extends GuiTestBase {

    private AddressBook initialData = generateInitialData();

    private AddressBook generateInitialData() {//TODO: create a better set of sample data
        AddressBook ab = new AddressBook();
        TypicalData typicalData = new TypicalData();
        ab.addPerson(typicalData.ALICE);
        ab.addPerson(typicalData.BENSON);
        ab.addPerson(typicalData.CHARLIE);
        ab.addPerson(typicalData.DAN);
        ab.addPerson(typicalData.ELIZABETH);
        return ab;
    }

    class TypicalData{
        Person ALICE = new Person("Alice", "Brown", 1);
        Person BENSON = new Person("Benson", "Christopher Dean", 2);
        Person CHARLIE =new Person("Charlie", "Davidson", 3);
        Person DAN = new Person("Dan", "Edwards", 4);
        Person ELIZABETH = new Person("Elizabeth", "F. Green", 5);
    }

    @Override
    protected ReadOnlyAddressBook getInitialData() {
        return initialData;
    }

    @Override
    protected CloudAddressBook getInitialCloudData() {
        return TestUtil.generateCloudAddressBook(initialData);
    }

    //TODO: code above has been duplicated from KeyBindingGuiTest. To be unified later.
    
    @Test
    public void dragAndDrop_singlePersonCorrectDrag_listReordered() {
        TypicalData typicalData = new TypicalData();
        Person alice = typicalData.ALICE;
        Person benson = typicalData.BENSON;
        Person charlie = typicalData.CHARLIE;
        Person dan = typicalData.DAN;
        Person elizabeth = typicalData.ELIZABETH;
        assertTrue(personListPanel.containsInOrder(alice, benson, charlie, dan, elizabeth));

        // drag first person (Alice) and drop on Charles
        personListPanel.dragAndDrop(alice.getFirstName(), charlie.getFirstName());
        assertTrue(personListPanel.containsInOrder(benson, alice, charlie, dan, elizabeth));

        //drag last person and drop at the beginning
        personListPanel.dragAndDrop(elizabeth.getFirstName(), benson.getFirstName());
        assertTrue(personListPanel.containsInOrder(elizabeth, benson, alice, charlie, dan));
    }

}
