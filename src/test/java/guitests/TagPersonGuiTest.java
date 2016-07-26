package guitests;

import address.model.datatypes.AddressBook;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.PersonCardHandle;
import guitests.guihandles.TagPersonDialogHandle;
import org.junit.Test;

import java.util.Arrays;

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
    public void tagPerson_MultipleTags() {
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
    public void tagPerson_multipleTagsAndCancelDuringPendingState() {
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

    @Test
    public void tagPerson_severalTagSelectionChanges() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialogUsingShortcut();

        aliceTagDialog.enterSearchQuery("coll").acceptSuggestedTag();
        aliceTagDialog.enterSearchQuery("frie").acceptSuggestedTag();
        aliceTagDialog.enterSearchQuery("coll").acceptSuggestedTag();
        aliceTagDialog.enterSearchQuery("fam").acceptSuggestedTag();
        aliceTagDialog.enterSearchQuery("coll").acceptSuggestedTag();
        aliceTagDialog.enterSearchQuery("fam").acceptSuggestedTag();
        aliceTagDialog.enterSearchQuery("coll").acceptSuggestedTag();
        aliceTagDialog.close();

        aliceEditDialog.pressEnter();

        assertEquals("Tag: friends", alicePersonCard.getTags());
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));
        sleepForGracePeriod();
        assertEquals("Tag: friends", alicePersonCard.getTags());
        assertFalse(alicePersonCard.isShowingGracePeriod("Editing"));
    }

    @Test
    public void tagAndUntagPerson() {
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

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialogUsingShortcut();

        aliceTagDialogTwo.enterSearchQuery("coll").acceptSuggestedTag();
        aliceTagDialogTwo.close();

        aliceEditDialogTwo.pressEnter();

        assertEquals("", alicePersonCard.getTags());
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));
        sleepForGracePeriod();
        assertEquals("", alicePersonCard.getTags());
        assertFalse(alicePersonCard.isShowingGracePeriod("Editing"));
    }

    @Test
    public void tagPersonAndChangeTag() {
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

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialogUsingShortcut();

        aliceTagDialogTwo.enterSearchQuery("coll").acceptSuggestedTag();
        aliceTagDialogTwo.enterSearchQuery("frie").acceptSuggestedTag();
        aliceTagDialogTwo.enterSearchQuery("fam").acceptSuggestedTag();
        aliceTagDialogTwo.close();

        aliceEditDialogTwo.pressEnter();

        assertEquals("Tag: friends, Tag: family", alicePersonCard.getTags());
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));
        sleepForGracePeriod();
        assertEquals("Tag: friends, Tag: family", alicePersonCard.getTags());
        assertFalse(alicePersonCard.isShowingGracePeriod("Editing"));
    }

    @Test
    public void tagPersonAndChangeTag_cancelDuringPendingState() {
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

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialogUsingShortcut();

        aliceTagDialogTwo.enterSearchQuery("coll").acceptSuggestedTag();
        aliceTagDialogTwo.enterSearchQuery("frie").acceptSuggestedTag();
        aliceTagDialogTwo.close();

        aliceEditDialogTwo.pressEnter();

        assertEquals("Tag: friends", alicePersonCard.getTags());
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertEquals("Tag: colleagues", alicePersonCard.getTags());
        assertFalse(alicePersonCard.isShowingGracePeriod("Editing"));
    }

    @Test
    public void tagPerson_changeTagDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialogUsingShortcut();

        aliceTagDialog.enterSearchQuery("coll").acceptSuggestedTag();
        aliceTagDialog.close();

        aliceEditDialog.pressEnter();

        assertEquals("Tag: colleagues", alicePersonCard.getTags());
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialogUsingShortcut();

        aliceTagDialogTwo.enterSearchQuery("coll").acceptSuggestedTag();
        aliceTagDialogTwo.enterSearchQuery("frie").acceptSuggestedTag();
        aliceTagDialogTwo.close();

        aliceEditDialogTwo.pressEnter();

        assertEquals("Tag: friends", alicePersonCard.getTags());
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertEquals("", alicePersonCard.getTags());
        assertFalse(alicePersonCard.isShowingGracePeriod("Editing"));
    }

    @Test
    public void tagMultiplePersonsAccelerator() {
        personListPanel.clickOnMultiplePersons(Arrays.asList(td.alice, td.benson, td.charlie));
        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandle.enterSearchQuery("frie").acceptSuggestedTag();
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        assertEquals("Tag: friends", alicePersonCard.getTags());
        assertTrue(alicePersonCard.isShowingGracePeriod("Editing"));

        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        assertEquals("Tag: friends", bensonPersonCard.getTags());
        assertTrue(bensonPersonCard.isShowingGracePeriod("Editing"));

        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);
        assertEquals("Tag: friends", charliePersonCard.getTags());
        assertTrue(charliePersonCard.isShowingGracePeriod("Editing"));

        sleepForGracePeriod();

        assertEquals("Tag: friends", alicePersonCard.getTags());
        assertFalse(alicePersonCard.isShowingGracePeriod("Editing"));
        assertEquals("Tag: friends", bensonPersonCard.getTags());
        assertFalse(bensonPersonCard.isShowingGracePeriod("Editing"));
        assertEquals("Tag: friends", charliePersonCard.getTags());
        assertFalse(charliePersonCard.isShowingGracePeriod("Editing"));
    }
}
