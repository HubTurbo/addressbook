package guitests;

import address.model.datatypes.AddressBook;
import guitests.guihandles.PersonListPanelHandle;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class PersonListGuiTest extends GuiTestBase {

    @Override
    protected AddressBook getInitialData() {
        return td.book;
    }

    @Test
    public void dragAndDrop_singlePersonCorrectDrag_listReordered() {

        assertTrue(personListPanel.containsInOrder(td.alice, td.benson, td.charlie, td.dan, td.elizabeth));

        // drag first person (Alice) and drop on Charles
        personListPanel.dragAndDrop(td.alice.getFirstName(), td.charlie.getFirstName());
        assertTrue(personListPanel.containsInOrder(td.benson, td.alice, td.charlie, td.dan, td.elizabeth));

        // drag a card (Charlie) to the top
        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(1); //ensure the destination card is visible
        personListPanel.dragAndDrop(td.charlie.getFirstName(), td.benson.getFirstName());
        assertTrue(personListPanel.containsInOrder(td.charlie, td.benson, td.alice, td.dan, td.elizabeth));

        //drag the person at the bottom and drop at the middle
        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(5); //Make the target card visible
        personListPanel.dragAndDrop(td.elizabeth.getFirstName(), td.alice.getFirstName());
        assertTrue(personListPanel.containsInOrder(td.charlie, td.benson, td.elizabeth, td.alice, td.dan));

        //drag the person at the middle and drop at the bottom
        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(2);
        personListPanel.edgeDrag(td.elizabeth.getFirstName(), PersonListPanelHandle.Direction.DOWN, 3, TimeUnit.SECONDS);
        assertTrue(personListPanel.containsInOrder(td.charlie, td.benson, td.alice, td.dan, td.elizabeth));

        //drag the person at the bottom and drop at the top
        personListPanel.use_LIST_JUMP_TO_INDEX_SHORTCUT(5);
        personListPanel.edgeDrag(td.elizabeth.getFirstName(), PersonListPanelHandle.Direction.UP, 5, TimeUnit.SECONDS);
        assertTrue(personListPanel.containsInOrder(td.elizabeth, td.charlie, td.benson, td.alice, td.dan));

    }

    @Test
    public void dragAndDrop_singlePersonWrongDrag_listUnchanged() {
        assertTrue(personListPanel.containsInOrder(td.alice, td.benson, td.charlie, td.dan, td.elizabeth));

        personListPanel.dragAndDrop(td.charlie.getFirstName(), td.charlie.getFirstName());
        assertTrue(personListPanel.containsInOrder(td.alice, td.benson, td.charlie, td.dan, td.elizabeth));

        //TODO: test for Dropping outside list
    }

    @Test
    public void dragAndDrop_multiplePersonCorrectDrag_listReordered() {
        //TODO: implement this
    }

    @Test
    public void dragAndDrop_multiplePersonWrongDrag_listUnchanged() {
        //TODO: implement this
    }

}
