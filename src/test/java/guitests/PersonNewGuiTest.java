package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import address.testutil.PersonBuilder;
import address.testutil.TestUtil;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.HeaderStatusBarHandle;
import guitests.guihandles.PersonCardHandle;
import org.junit.Test;

import java.util.Optional;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * System tests for 'New person' feature.
 */
public class PersonNewGuiTest extends GuiTestBase {

    private Person fionaEdited = new PersonBuilder(td.fiona.copy()).withLastName("Chong").withGithubUsername("chong")
                                                                   .build();

    @Override
    protected AddressBook getInitialData() {
        return td.book;
    }

    @Test
    public void addPerson_createAndDeleteInGracePeriod() {
        //Add Fiona
        EditPersonDialogHandle addPersonDialog = personListPanel.clickNew();
        addPersonDialog.enterNewValues(td.fiona).clickOk();

        PersonCardHandle fionaCard = personListPanel.navigateToPerson(td.fiona);
        assertMatching(fionaCard, td.fiona);

        //Delete Fiona
        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        assertFalse(personListPanel.contains(td.fiona));

        //Add George
        addPersonDialog = personListPanel.clickNew();
        addPersonDialog.enterNewValues(td.george).clickOk();
        PersonCardHandle georgeCard = personListPanel.navigateToPerson(td.george);
        assertTrue(personListPanel.isListMatching(TestUtil.addPersonsToList(td.getTestData(), td.george)));
    }

    @Test
    public void addPerson_addTwoPersons() {

        EditPersonDialogHandle addPersonDialog = personListPanel.clickNew();
        addPersonDialog.enterNewValues(td.fiona).clickOk();
        PersonCardHandle fionaCard = personListPanel.navigateToPerson(td.fiona);
        assertMatching(fionaCard, td.fiona);

        addPersonDialog = personListPanel.clickNew();
        addPersonDialog.enterNewValues(td.george).clickOk();
        PersonCardHandle georgeCard = personListPanel.navigateToPerson(td.george);
        assertMatching(georgeCard, td.george);

        assertTrue(personListPanel.isListMatching(TestUtil.addPersonsToList(td.getTestData(), td.fiona, td.george)));
    }

    @Test
    public void addPerson_cancelDialog() {
        EditPersonDialogHandle addPersonDialog = personListPanel.clickNew();
        addPersonDialog.enterNewValues(td.fiona).clickCancel();
        assertTrue(personListPanel.isListMatching(td.getTestData()));
    }

}
