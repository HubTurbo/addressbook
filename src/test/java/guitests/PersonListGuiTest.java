package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.ReadOnlyAddressBook;
import address.model.datatypes.person.Person;
import address.sync.cloud.model.CloudAddressBook;
import address.util.TestUtil;
import javafx.scene.control.Label;
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
    public void dragAndDrop_singlePerson() {
        TypicalData typicalData = new TypicalData();
        Label aliceNameLabel = getNameLabelOf(typicalData.ALICE.getFirstName());
        Label bensonIdLabel = getNameLabelOf(typicalData.BENSON.getFirstName());
        assertTrue(aliceNameLabel.localToScreen(0, 0).getY() < bensonIdLabel.localToScreen(0, 0).getY());
        guiRobot.drag(typicalData.ALICE.getFirstName()).dropTo(typicalData.CHARLIE.getFirstName());// drag from first to start of 3rd (slightly further down between 2nd and 3rd)

        Label aliceNameLabel2 = getNameLabelOf(typicalData.ALICE.getFirstName());
        Label bensonIdLabel2 = getNameLabelOf(typicalData.BENSON.getFirstName());
        assertTrue(aliceNameLabel2.localToScreen(0, 0).getY() > bensonIdLabel2.localToScreen(0, 0).getY());
    }

    private Label getNameLabelOf(String name) {
        return (Label) guiRobot.lookup(name).tryQuery().get();
    }
}
