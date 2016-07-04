package guitests;

import address.keybindings.Bindings;
import address.model.datatypes.AddressBook;
import address.model.datatypes.ReadOnlyAddressBook;
import address.model.datatypes.person.Person;
import javafx.scene.input.KeyCode;
import org.junit.Test;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.hasText;

/**
 * Tests key bindings through the GUI
 */
public class KeyBindingsGuiTest extends GuiTestBase {

    private final Bindings bindings = new Bindings();

    @Override
    protected ReadOnlyAddressBook getInitialData() {
        AddressBook ab = new AddressBook();
        ab.addPerson(new Person("Person1", "Lastname1", 1));
        ab.addPerson(new Person("Person2", "Lastname2", 2));
        ab.addPerson(new Person("Person3", "Lastname3", 3));
        ab.addPerson(new Person("Person4", "Lastname4", 4));
        ab.addPerson(new Person("Person5", "Lastname5", 5));
        return ab;
        //TODO: create a better set of sample data
    }

    @Test
    public void keyBindings(){

        //======= shortcuts =======================

        push(bindings.LIST_ENTER_SHORTCUT);
        verifyPersonSelected("Person1", "Lastname1");

        push(KeyCode.CONTROL, KeyCode.DIGIT3);
        verifyPersonSelected("Person3", "Lastname3");

        //======= sequences =========================

        pushKeySequence(bindings.LIST_GOTO_BOTTOM_SEQUENCE);
        verifyPersonSelected("Person5", "Lastname5");

        pushKeySequence(bindings.LIST_GOTO_TOP_SEQUENCE);
        verifyPersonSelected("Person1", "Lastname1");


        //======= accelerators =======================

        push(KeyCode.CONTROL, KeyCode.DIGIT3);
        push(bindings.PERSON_EDIT_ACCELERATOR);
        verifyEditWindowOpened("Person3", "Lastname3");

        push(bindings.PERSON_DELETE_ACCELERATOR);
        verifyPersonDeleted("Person3", "Lastname3");

        //TODO: test tag, file open, new, save, save as, cancel

        //======== others ============================

        pushKeySequence(bindings.LIST_GOTO_BOTTOM_SEQUENCE);
        push(KeyCode.UP);
        verifyPersonSelected("Person4", "Lastname4");

        push(KeyCode.DOWN);
        verifyPersonSelected("Person5", "Lastname5");

        //======== hotkeys ============================

        //TODO: test hotkeys 

    }


    private void verifyPersonDeleted(String firstName, String lastName) {
        //TODO: implement this
    }

    private void verifyEditWindowOpened(String firstName, String lastName) {
        targetWindow("Edit Person");
        verifyThat("#firstNameField", hasText(firstName));
        verifyThat("#lastNameField", hasText(lastName));
        clickOn("Cancel");
    }

    private void verifyPersonSelected(String firstName, String lastName) {
        push(bindings.PERSON_EDIT_ACCELERATOR);
        verifyEditWindowOpened(firstName, lastName);
    }

}
