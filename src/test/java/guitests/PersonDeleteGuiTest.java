package guitests;

import address.model.datatypes.AddressBook;
import address.testutil.TestUtil;
import guitests.guihandles.HeaderStatusBarHandle;
import guitests.guihandles.PersonCardHandle;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO: Finish the full use cases of delete person
 * System tests for 'Delete person' feature.
 */
public class PersonDeleteGuiTest extends GuiTestBase {

    @Override
    protected AddressBook getInitialData() {
        return td.book;
    }

    @Test
    public void deleteMultiplePersons_usingAccelerator() {
        personListPanel.selectCards(td.benson, td.dan);
        assertEquals(2, personListPanel.getSelectedCardSize());
        assertTrue(personListPanel.isSelected(td.benson));
        assertTrue(personListPanel.isSelected(td.dan));
        sleep(1, TimeUnit.SECONDS);
        personListPanel.use_PERSON_DELETE_ACCELERATOR();

        assertTrue(personListPanel.isListMatching(TestUtil.removePersonsFromList(td.getTestData(), td.benson, td.dan)));
    }


    @Test
    public void deletePerson_deleteWholeList() {
        personListPanel.selectCards(td.getTestData());
        assertEquals(td.getTestData().length, personListPanel.getSelectedCardSize());

        personListPanel.use_PERSON_DELETE_ACCELERATOR();

        assertEquals(0, personListPanel.getSelectedCardSize());
    }

    @Test
    public void deletePerson_deleteUsingAccelerator() {
        PersonCardHandle bensonCard = personListPanel.navigateToPerson(td.benson);
        assertTrue(personListPanel.isOnlySelected(td.benson));

        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        assertTrue(bensonCard.isShowingGracePeriod("Deleting"));

        sleepForGracePeriod();

        assertFalse(personListPanel.isAnyCardShowingGracePeriod());
        assertTrue(personListPanel.isListMatching(TestUtil.removePersonsFromList(td.getTestData(), td.benson)));
        assertEquals(HeaderStatusBarHandle.formatDeleteSuccessMessage(td.benson.fullName(), Optional.empty()),
                     statusBar.getText());
    }

    @Test
    public void deletePerson_deleteUsingContextMenu() {
        assertTrue(personListPanel.isOnlySelected(td.charlie));

        personListPanel.rightClickOnPerson(td.charlie).clickOnContextMenuDelete();

        assertTrue(personListPanel.isListMatching(TestUtil.removePersonsFromList(td.getTestData(), td.charlie)));
    }

    @Test
    public void deletePerson_deleteUsingDeleteButton() {
        assertTrue(personListPanel.isOnlySelected(td.dan));

        personListPanel.clickOnPerson(td.dan);
        personListPanel.clickDelete();

        assertTrue(personListPanel.isListMatching(TestUtil.removePersonsFromList(td.getTestData(), td.dan)));

    }

}
