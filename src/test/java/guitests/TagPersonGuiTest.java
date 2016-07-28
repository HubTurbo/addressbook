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
    public void tagPersonThroughPersonEditDialog() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: colleagues", alicePersonCard);
    }

    @Test
    public void tagPersonThroughPersonEditDialog_alreadyHasOtherTags() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("frien");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: friends", alicePersonCard);


        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialog();

        aliceTagDialogTwo.searchAndAcceptTags("coll");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard);
    }

    @Test
    public void tagPersonThroughPersonEditDialog_cancelDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        cancelDuringGracePeriod("Tag: colleagues", "", alicePersonCard);
    }

    @Test
    public void tagPersonThroughPersonEditDialog_multipleTags() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll", "frie");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: colleagues, Tag: friends", alicePersonCard);
    }

    @Test
    public void tagPersonThroughPersonEditDialog_multipleTagsAndCancelDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll", "frie");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        cancelDuringGracePeriod("Tag: colleagues, Tag: friends", "", alicePersonCard);
    }

    @Test
    public void tagPersonThroughPersonEditDialog_severalTagSelectionChanges() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll", "frie", "coll", "fam", "coll", "fam", "coll");
        aliceTagDialog.close();

        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: friends", alicePersonCard);
    }

    @Test
    public void tagAndUntagPersonThroughPersonEditDialog() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: colleagues", alicePersonCard);

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialog();

        aliceTagDialogTwo.searchAndAcceptTags("coll");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        waitForGracePeriod("", alicePersonCard);
    }

    @Test
    public void tagAndChangeTagThroughPersonEditDialog() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: colleagues", alicePersonCard);

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll", "frie");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        waitForGracePeriod("Tag: friends", alicePersonCard);
    }

    @Test
    public void tagAndChangeTagThroughPersonEditDialog_cancelDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: colleagues", alicePersonCard);

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialog();

        aliceTagDialogTwo.searchAndAcceptTags("coll", "frie");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        cancelDuringGracePeriod("Tag: friends", "Tag: colleagues", alicePersonCard);
    }

    @Test
    public void tagPersonThroughPersonEditDialog_changeTagDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        assertTagsBeforeGracePeriod("Tag: colleagues", alicePersonCard);

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialog();

        aliceTagDialogTwo.searchAndAcceptTags("coll", "frie");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        cancelDuringGracePeriod("Tag: friends", "", alicePersonCard);
    }

    @Test
    public void tagPersonAccelerator_multipleTags() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        TagPersonDialogHandle tagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        tagDialogHandle.searchAndAcceptTags("frie", "coll");
        tagDialogHandle.pressEnter();

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard);
    }

    @Test
    public void tagPersonAccelerator_multipleTagsCancelDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        TagPersonDialogHandle tagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        tagDialogHandle.searchAndAcceptTags("frie", "coll");
        tagDialogHandle.pressEnter();

        cancelDuringGracePeriod("Tag: friends, Tag: colleagues", "", alicePersonCard);
    }

    @Test
    public void tagPersonAccelerator() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        TagPersonDialogHandle tagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        tagDialogHandle.searchAndAcceptTags("frie");
        tagDialogHandle.pressEnter();

        waitForGracePeriod("Tag: friends", alicePersonCard);
    }

    @Test
    public void tagPersonAccelerator_noSelectedPerson() {
        personListPanel.use_PERSON_TAG_ACCELERATOR();
        assertTrue(personListPanel.isNoSelectedPersonDialogShown());
    }

    private void assertTagsBeforeGracePeriod(String expectedTags, PersonCardHandle... personCardHandles) {
        for (PersonCardHandle personCardHandle : personCardHandles) {
            assertEquals(expectedTags, personCardHandle.getTags());
            assertTrue(personCardHandle.isShowingGracePeriod("Editing"));
        }
    }

    private void assertTagsAfterGracePeriod(String expectedTags, PersonCardHandle... personCardHandles) {
        for (PersonCardHandle personCardHandle : personCardHandles) {
            assertEquals(expectedTags, personCardHandle.getTags());
            assertFalse(personCardHandle.isShowingGracePeriod("Editing"));
        }
    }

    private void waitForGracePeriod(String expectedTags, PersonCardHandle... personCardHandles) {
        assertTagsBeforeGracePeriod(expectedTags, personCardHandles);
        sleepForGracePeriod();
        assertTagsAfterGracePeriod(expectedTags, personCardHandles);
    }

    private void cancelDuringGracePeriod(String expectedTagsBeforeCancel, String expectedTagsAfterCancel,
                                         PersonCardHandle... personCardHandles) {
        assertTagsBeforeGracePeriod(expectedTagsBeforeCancel, personCardHandles);
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertTagsAfterGracePeriod(expectedTagsAfterCancel, personCardHandles);
    }
}
