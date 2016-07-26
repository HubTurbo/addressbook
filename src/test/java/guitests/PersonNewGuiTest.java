package guitests;

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
 * System tests for 'New person' feature.
 */
public class PersonNewGuiTest extends GuiTestBase {

    @Override
    protected AddressBook getInitialData() {
        return td.book;
    }

    @Test
    public void cancelOperation_usingAccelerator() {

        //New
        EditPersonDialogHandle addPersonDialog = personListPanel.clickNew();
        assertTrue(addPersonDialog.isShowingEmptyEditDialog());

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
