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

        searchAndAcceptTags(aliceTagDialog, "coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: colleagues", alicePersonCard);
    }

    @Test
    public void tagPerson_alreadyHasOtherTags() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialogUsingShortcut();

        searchAndAcceptTags(aliceTagDialog, "frien");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: friends", alicePersonCard);


        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialogUsingShortcut();

        searchAndAcceptTags(aliceTagDialogTwo, "coll");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard);
    }

    @Test
    public void tagPerson_cancelDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialogUsingShortcut();

        searchAndAcceptTags(aliceTagDialog, "coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        cancelDuringGracePeriod("Tag: colleagues", "", alicePersonCard);
    }

    @Test
    public void tagPerson_MultipleTags() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialogUsingShortcut();

        searchAndAcceptTags(aliceTagDialog, "coll", "frie");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: colleagues, Tag: friends", alicePersonCard);
    }

    @Test
    public void tagPerson_multipleTagsAndCancelDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialogUsingShortcut();

        searchAndAcceptTags(aliceTagDialog, "coll", "frie");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        cancelDuringGracePeriod("Tag: colleagues, Tag: friends", "", alicePersonCard);
    }

    @Test
    public void tagPerson_severalTagSelectionChanges() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialogUsingShortcut();

        searchAndAcceptTags(aliceTagDialog, "coll", "frie", "coll", "fam", "coll", "fam", "coll");
        aliceTagDialog.close();

        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: friends", alicePersonCard);
    }

    private void searchAndAcceptTags(TagPersonDialogHandle aliceTagDialog, String... tagQueries) {
        for (String tagQuery : tagQueries) {
            aliceTagDialog.searchAndAcceptTag(tagQuery);
        }
    }

    @Test
    public void tagAndUntagPerson() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialogUsingShortcut();

        searchAndAcceptTags(aliceTagDialog, "coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: colleagues", alicePersonCard);

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialogUsingShortcut();

        searchAndAcceptTags(aliceTagDialogTwo, "coll");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        waitForGracePeriod("", alicePersonCard);
    }

    @Test
    public void tagPersonAndChangeTag() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialogUsingShortcut();

        searchAndAcceptTags(aliceTagDialog, "coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: colleagues", alicePersonCard);

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialogUsingShortcut();

        searchAndAcceptTags(aliceTagDialog, "coll", "frie", "fam");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        waitForGracePeriod("Tag: friends, Tag: family", alicePersonCard);
    }

    @Test
    public void tagPersonAndChangeTag_cancelDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialogUsingShortcut();

        searchAndAcceptTags(aliceTagDialog, "coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: colleagues", alicePersonCard);

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialogUsingShortcut();

        searchAndAcceptTags(aliceTagDialogTwo, "coll", "frie");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        cancelDuringGracePeriod("Tag: friends", "Tag: colleagues", alicePersonCard);
    }

    @Test
    public void tagPerson_changeTagDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialogUsingShortcut();

        searchAndAcceptTags(aliceTagDialog, "coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        assertTagsBeforeGracePeriod("Tag: colleagues", alicePersonCard);

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialogUsingShortcut();

        searchAndAcceptTags(aliceTagDialogTwo, "coll", "frie");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        cancelDuringGracePeriod("Tag: friends", "", alicePersonCard);
    }

    @Test
    public void tagMultiplePersonsAccelerator() {
        personListPanel.clickOnMultiplePersons(Arrays.asList(td.alice, td.benson, td.charlie));
        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        searchAndAcceptTags(multiplePersonsTagDialogHandle, "frie");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        waitForGracePeriod("Tag: friends", alicePersonCard, bensonPersonCard, charliePersonCard);
    }

    @Test
    public void tagMultiplePersonsAccelerator_multipleTags() {
        personListPanel.clickOnMultiplePersons(Arrays.asList(td.alice, td.benson, td.charlie));
        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        searchAndAcceptTags(multiplePersonsTagDialogHandle, "frie", "coll");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard, bensonPersonCard, charliePersonCard);
    }

    @Test
    public void tagAndUnTagMultiplePersonsAccelerator() {
        personListPanel.clickOnMultiplePersons(Arrays.asList(td.alice, td.benson, td.charlie));
        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        searchAndAcceptTags(multiplePersonsTagDialogHandle, "frie");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        waitForGracePeriod("Tag: friends", alicePersonCard, bensonPersonCard, charliePersonCard);

        assertSelectedCardHandles(alicePersonCard, bensonPersonCard, charliePersonCard);
        TagPersonDialogHandle multiplePersonsTagDialogHandleTwo = personListPanel.use_PERSON_TAG_ACCELERATOR();
        searchAndAcceptTags(multiplePersonsTagDialogHandleTwo, "frie");
        multiplePersonsTagDialogHandleTwo.pressEnter();

        waitForGracePeriod("", alicePersonCard, bensonPersonCard, charliePersonCard);
    }

    @Test
    public void tagAndUnTagMultiplePersonsAccelerator_multipleTags() {
        personListPanel.clickOnMultiplePersons(Arrays.asList(td.alice, td.benson, td.charlie));
        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        searchAndAcceptTags(multiplePersonsTagDialogHandle, "frie", "coll");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard, bensonPersonCard, charliePersonCard);

        assertSelectedCardHandles(alicePersonCard, bensonPersonCard, charliePersonCard);
        TagPersonDialogHandle multiplePersonsTagDialogHandleTwo = personListPanel.use_PERSON_TAG_ACCELERATOR();
        searchAndAcceptTags(multiplePersonsTagDialogHandleTwo, "frie");
        multiplePersonsTagDialogHandleTwo.pressEnter();

        waitForGracePeriod("Tag: colleagues", alicePersonCard, bensonPersonCard, charliePersonCard);
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

    private void assertSelectedCardHandles(PersonCardHandle... personCardHandles) {
        for (PersonCardHandle personCardHandle : personCardHandles) {
            assertTrue(personListPanel.getSelectedCards().contains(personCardHandle));
        }
    }
}
