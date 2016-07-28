package guitests;

import address.model.datatypes.AddressBook;
import guitests.guihandles.HeaderStatusBarHandle;
import guitests.guihandles.PersonCardHandle;
import org.junit.Test;

import java.util.Optional;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

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
    public void deleteMultiplePerson_usingAccelerator() {
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

        //assertEquals(0, personListPanel.getSelectedCardSize()); Wait for #571 to be fixed.
        assertFalse(personListPanel.isAnyCardShowingGracePeriod());
        assertTrue(personListPanel.isListMatching(td.alice, td.charlie, td.elizabeth));
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
}
