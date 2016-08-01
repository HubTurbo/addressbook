package guitests;

import address.model.datatypes.AddressBook;
import address.testutil.TestUtil;
import guitests.guihandles.HeaderStatusBarHandle;
import guitests.guihandles.PersonCardHandle;
import org.junit.Test;

import java.util.Optional;

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

        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        PersonCardHandle danCard = personListPanel.navigateToPerson(td.dan);
        assertTrue(danCard.isShowingGracePeriod("Deleting"));
        PersonCardHandle bensonCard = personListPanel.navigateToPerson(td.benson);
        assertTrue(bensonCard.isShowingGracePeriod("Deleting"));
        sleepForGracePeriod();

        assertFalse(personListPanel.isAnyCardShowingGracePeriod());
        assertTrue(personListPanel.isListMatching(TestUtil.removePersonsFromList(td.getTestData(), td.benson, td.dan)));
    }

    @Test
    public void deleteMultiplePersons_cancelUsingAccelerator() {
        personListPanel.selectCards(td.alice, td.charlie, td.dan);
        assertEquals(3, personListPanel.getSelectedCardSize());
        assertTrue(personListPanel.isSelected(td.alice));
        assertTrue(personListPanel.isSelected(td.charlie));
        assertTrue(personListPanel.isSelected(td.dan));

        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        PersonCardHandle aliceCard = personListPanel.navigateToPerson(td.alice);
        assertTrue(aliceCard.isShowingGracePeriod("Deleting"));
        PersonCardHandle charlieCard = personListPanel.navigateToPerson(td.charlie);
        assertTrue(charlieCard.isShowingGracePeriod("Deleting"));
        PersonCardHandle danCard = personListPanel.navigateToPerson(td.dan);
        assertTrue(danCard.isShowingGracePeriod("Deleting"));

        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();

        assertFalse(personListPanel.isAnyCardShowingGracePeriod());
        assertTrue(personListPanel.isListMatching(td.getTestData()));
    }

    @Test
    public void deleteMultiplePersons_cancelUsingContextMenu() {
        personListPanel.selectCards(td.alice, td.charlie, td.dan);
        assertEquals(3, personListPanel.getSelectedCardSize());
        assertTrue(personListPanel.isSelected(td.alice));
        assertTrue(personListPanel.isSelected(td.charlie));
        assertTrue(personListPanel.isSelected(td.dan));
        personListPanel.moveCursor(td.dan); //To prepare right clicking the context menu

        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        //Omit testing of pending state, as grace period is too short to cancel through context menu.

        personListPanel.rightClickOnPerson(td.dan).clickOnContextMenuCancel();

        sleepForGracePeriod();

        assertTrue(personListPanel.isListMatching(TestUtil.removePersonsFromList(td.getTestData(), td.alice, td.charlie)));
    }

    @Test
    public void deletePerson_deleteWholeList() {
        personListPanel.selectCards(td.getTestData());
        assertEquals(td.getTestData().length, personListPanel.getSelectedCardSize());

        personListPanel.use_PERSON_DELETE_ACCELERATOR();

        //assertTrue(personListPanel.isEntireListShowingGracePeriod("Deleting")); TODO: grace period too short to check
        sleepForGracePeriod();

        assertEquals(0, personListPanel.getSelectedCardSize());
    }

    @Test
    public void deletePerson_cancelDeleteOperationUsingAccelerator() {

        PersonCardHandle aliceCard = personListPanel.selectCard(td.alice);
        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        assertTrue(aliceCard.isShowingGracePeriod("Deleting"));

        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertFalse(aliceCard.isShowingGracePeriod("Deleting"));
        assertEquals(HeaderStatusBarHandle.formatDeleteCancelledMessage(td.alice.fullName(), Optional.empty()),
                     statusBar.getText());
    }

    @Test
    public void deletePerson_cancelDeleteOperationUsingContextMenu() {

        PersonCardHandle aliceCard = personListPanel.selectCard(td.alice);
        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        assertTrue(aliceCard.isShowingGracePeriod("Deleting"));

        personListPanel.rightClickOnPerson(td.alice).clickOnContextMenuCancel();
        assertFalse(aliceCard.isShowingGracePeriod("Deleting"));
        assertEquals(HeaderStatusBarHandle.formatDeleteCancelledMessage(td.alice.fullName(), Optional.empty()),
                statusBar.getText());
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
        PersonCardHandle charlieCard = personListPanel.navigateToPerson(td.charlie);
        assertTrue(personListPanel.isOnlySelected(td.charlie));

        personListPanel.rightClickOnPerson(td.charlie).clickOnContextMenuDelete();
        assertTrue(charlieCard.isShowingGracePeriod("Deleting"));

        sleepForGracePeriod();

        assertFalse(personListPanel.isAnyCardShowingGracePeriod());
        assertTrue(personListPanel.isListMatching(TestUtil.removePersonsFromList(td.getTestData(), td.charlie)));
        assertEquals(HeaderStatusBarHandle.formatDeleteSuccessMessage(td.charlie.fullName(), Optional.empty()),
                statusBar.getText());
    }

    @Test
    public void deletePerson_deleteUsingDeleteButton() {
        PersonCardHandle charlieCard = personListPanel.navigateToPerson(td.dan);
        assertTrue(personListPanel.isOnlySelected(td.dan));

        personListPanel.clickOnPerson(td.dan);
        personListPanel.clickDelete();
        assertTrue(charlieCard.isShowingGracePeriod("Deleting"));

        sleepForGracePeriod();

        assertFalse(personListPanel.isAnyCardShowingGracePeriod());
        assertTrue(personListPanel.isListMatching(TestUtil.removePersonsFromList(td.getTestData(), td.dan)));
        assertEquals(HeaderStatusBarHandle.formatDeleteSuccessMessage(td.dan.fullName(), Optional.empty()),
                statusBar.getText());

    }

}
