package guitests;

import address.model.datatypes.AddressBook;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.PersonCardHandle;
import guitests.guihandles.TagPersonDialogHandle;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TagPersonGuiTest extends GuiTestBase {
    @Override
    protected AddressBook getInitialData() {
        return td.book;
    }

    @Test
    public void tagPerson() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialogUsingShortcut();

        aliceTagDialog.enterSearchQuery("coll").acceptSuggestedTag();
        aliceTagDialog.close();

        aliceEditDialog.pressEnter();

        assertEquals("Tag: colleagues", alicePersonCard.getTags());
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));
        sleepForGracePeriod();
        assertEquals("Tag: colleagues", alicePersonCard.getTags());
        assertFalse(alicePersonCard.isShowingGracePeriod("Editing"));
    }

    @Test
    public void tagPerson_cancelDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialogUsingShortcut();

        aliceTagDialog.enterSearchQuery("coll").acceptSuggestedTag();
        aliceTagDialog.close();

        aliceEditDialog.pressEnter();

        assertEquals("Tag: colleagues", alicePersonCard.getTags());
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertEquals("", alicePersonCard.getTags());
        assertFalse(alicePersonCard.isShowingGracePeriod("Editing"));
    }

    @Test
    public void tagPersonMultipleTags() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialogUsingShortcut();

        aliceTagDialog.enterSearchQuery("coll").acceptSuggestedTag();
        aliceTagDialog.enterSearchQuery("frie").acceptSuggestedTag();
        aliceTagDialog.close();

        aliceEditDialog.pressEnter();

        assertEquals("Tag: colleagues, Tag: friends", alicePersonCard.getTags());
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));
        sleepForGracePeriod();
        assertEquals("Tag: colleagues, Tag: friends", alicePersonCard.getTags());
        assertFalse(alicePersonCard.isShowingGracePeriod("Editing"));
    }

    @Test
    public void tagPersonMultipleTags_cancelDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialogUsingShortcut();

        aliceTagDialog.enterSearchQuery("coll").acceptSuggestedTag();
        aliceTagDialog.enterSearchQuery("frie").acceptSuggestedTag();
        aliceTagDialog.close();

        aliceEditDialog.pressEnter();

        assertEquals("Tag: colleagues, Tag: friends", alicePersonCard.getTags());
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertEquals("", alicePersonCard.getTags());
        assertFalse(alicePersonCard.isShowingGracePeriod("Editing"));
    }
}
