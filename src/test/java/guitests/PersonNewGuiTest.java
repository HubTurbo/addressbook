package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import address.testutil.PersonBuilder;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.PersonCardHandle;
import org.junit.Test;


import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

/**
 * System tests for 'New person' feature.
 */
public class PersonNewGuiTest extends GuiTestBase {

    @Override
    protected AddressBook getInitialData() {
        return td.book;
    }

    @Test
    public void addPerson_createAndDelete() {
        //Create Person A
        EditPersonDialogHandle addPersonDialog = personListPanel.clickNew();

        Person pandaWong = new PersonBuilder("Panda", "Wong")
                .withStreet("Chengdu Panda Street").withCity("Chengdu").withPostalCode("PANDA")
                .withBirthday("01.01.1979").withGithubUsername("panda").withTags(td.colleagues, td.friends).build();
        addPersonDialog.enterNewValues(pandaWong).clickOk();

        PersonCardHandle pandaWongCardHandle = personListPanel.navigateToPerson(pandaWong);
        assertTrue(pandaWongCardHandle.isShowingGracePeriod("Adding"));
        assertMatching(pandaWongCardHandle, pandaWong);

        //Delete Person A
        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        assertMatching(pandaWongCardHandle, pandaWong);
        sleepForGracePeriod();
        assertFalse(personListPanel.contains(pandaWong));

        //Create Person B
        addPersonDialog = personListPanel.clickNew();
        Person ipMan = new PersonBuilder("IP", "Man")
                .withStreet("Kungfu street 51").withCity("Beijing").withPostalCode("K4UWIN")
                .withBirthday("01.01.1970").withGithubUsername("ipman").withTags(td.colleagues).build();
        addPersonDialog.enterNewValues(ipMan).clickOk();

        PersonCardHandle personCardHandle = personListPanel.navigateToPerson(ipMan);
        assertTrue(personCardHandle.isShowingGracePeriod("Adding"));
        sleepForGracePeriod();
        assertFalse(personListPanel.contains(pandaWong)); //Make sure Person A doesn't appear again.
        assertTrue(personListPanel.contains(ipMan));
    }

    @Test
    public void cancelOperation() {

        //New
        EditPersonDialogHandle addPersonDialog = personListPanel.clickNew();

        Person pandaWong = new PersonBuilder("Panda", "Wong")
                .withStreet("Chengdu Panda Street").withCity("Chengdu").withPostalCode("PANDA")
                .withBirthday("01.01.1979").withGithubUsername("panda").withTags(td.colleagues, td.friends).build();
        addPersonDialog.enterNewValues(pandaWong).clickOk();


        PersonCardHandle pandaWongCardHandle = personListPanel.navigateToPerson(pandaWong);
        assertTrue(pandaWongCardHandle.isShowingGracePeriod("Adding"));
        assertMatching(pandaWongCardHandle, pandaWong); //Ensure correct state before cancelling.

        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertNull(personListPanel.getPersonCardHandle(pandaWong));
        assertFalse(pandaWongCardHandle.isShowingGracePeriod("Adding"));
        assertEquals("Add Person [ Panda Wong ] was cancelled.", statusBar.getText());
    }
}
