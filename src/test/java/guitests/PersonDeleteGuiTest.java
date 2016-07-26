package guitests;

import address.model.datatypes.AddressBook;
import guitests.guihandles.PersonCardHandle;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

/**
 * System tests for 'Delete person' feature.
 */
public class PersonDeleteGuiTest extends GuiTestBase {

    @Override
    protected AddressBook getInitialData() {
        return td.book;
    }

    @Test
    public void cancelOperation_usingAccelerator() {

        PersonCardHandle aliceCard = personListPanel.selectCard(td.alice);
        personListPanel.use_PERSON_DELETE_ACCELERATOR();
        assertTrue(aliceCard.isShowingGracePeriod("Deleting"));

        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertFalse(aliceCard.isShowingGracePeriod("Deleting"));
        assertEquals(statusBar.getText(), "Delete Person [ " + aliceCard.getFirstName() + " "
                     + aliceCard.getLastName() + " ] was cancelled.");
    }
}
