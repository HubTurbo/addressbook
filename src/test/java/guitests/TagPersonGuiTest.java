package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.PersonCardHandle;
import guitests.guihandles.TagPersonDialogHandle;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

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

        searchAndAcceptTags(aliceTagDialog, "coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: colleagues", alicePersonCard);
    }

    @Test
    public void tagPersonThroughPersonEditDialog_alreadyHasOtherTags() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        searchAndAcceptTags(aliceTagDialog, "frien");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: friends", alicePersonCard);


        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialog();

        searchAndAcceptTags(aliceTagDialogTwo, "coll");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard);
    }

    @Test
    public void tagPersonThroughPersonEditDialog_cancelDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        searchAndAcceptTags(aliceTagDialog, "coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        cancelDuringGracePeriod("Tag: colleagues", "", alicePersonCard);
    }

    @Test
    public void tagPersonThroughPersonEditDialog_multipleTags() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        searchAndAcceptTags(aliceTagDialog, "coll", "frie");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: colleagues, Tag: friends", alicePersonCard);
    }

    @Test
    public void tagPersonThroughPersonEditDialog_multipleTagsAndCancelDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        searchAndAcceptTags(aliceTagDialog, "coll", "frie");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        cancelDuringGracePeriod("Tag: colleagues, Tag: friends", "", alicePersonCard);
    }

    @Test
    public void tagPersonThroughPersonEditDialog_severalTagSelectionChanges() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        searchAndAcceptTags(aliceTagDialog, "coll", "frie", "coll", "fam", "coll", "fam", "coll");
        aliceTagDialog.close();

        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: friends", alicePersonCard);
    }

    @Test
    public void tagAndUntagPersonThroughPersonEditDialog() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        searchAndAcceptTags(aliceTagDialog, "coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: colleagues", alicePersonCard);

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialog();

        searchAndAcceptTags(aliceTagDialogTwo, "coll");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        waitForGracePeriod("", alicePersonCard);
    }

    @Test
    public void tagAndChangeTagThroughPersonEditDialog() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        searchAndAcceptTags(aliceTagDialog, "coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: colleagues", alicePersonCard);

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialog();

        searchAndAcceptTags(aliceTagDialog, "coll", "frie");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        waitForGracePeriod("Tag: friends", alicePersonCard);
    }

    @Test
    public void tagAndChangeTagThroughPersonEditDialog_cancelDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        searchAndAcceptTags(aliceTagDialog, "coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: colleagues", alicePersonCard);

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialog();

        searchAndAcceptTags(aliceTagDialogTwo, "coll", "frie");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        cancelDuringGracePeriod("Tag: friends", "Tag: colleagues", alicePersonCard);
    }

    @Test
    public void tagPersonThroughPersonEditDialog_changeTagDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        searchAndAcceptTags(aliceTagDialog, "coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        assertTagsBeforeGracePeriod("Tag: colleagues", alicePersonCard);

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialog();

        searchAndAcceptTags(aliceTagDialogTwo, "coll", "frie");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        cancelDuringGracePeriod("Tag: friends", "", alicePersonCard);
    }

    @Test
    public void tagPersonAccelerator_multipleTags() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        TagPersonDialogHandle tagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        searchAndAcceptTags(tagDialogHandle, "frie", "coll");
        tagDialogHandle.pressEnter();

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard);
    }

    @Test
    public void tagPersonAccelerator_multipleTagsCancelDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        TagPersonDialogHandle tagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        searchAndAcceptTags(tagDialogHandle, "frie", "coll");
        tagDialogHandle.pressEnter();

        cancelDuringGracePeriod("Tag: friends, Tag: colleagues", "", alicePersonCard);
    }

    @Test
    public void tagPersonAccelerator() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        TagPersonDialogHandle tagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        searchAndAcceptTags(tagDialogHandle, "frie");
        tagDialogHandle.pressEnter();

        waitForGracePeriod("Tag: friends", alicePersonCard);
    }

    @Test
    public void tagPersonAccelerator_noSelectedPerson() {
        personListPanel.use_PERSON_TAG_ACCELERATOR();
        assertTrue(personListPanel.isNoSelectedPersonDialogShown());
    }

    @Test
    public void tagMultiplePersonsAccelerator() {
        clickOnMultiplePersons(td.alice, td.benson, td.charlie);

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
        clickOnMultiplePersons(td.alice, td.benson, td.charlie);
        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        searchAndAcceptTags(multiplePersonsTagDialogHandle, "frie", "coll");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard, bensonPersonCard, charliePersonCard);
    }

    @Test
    public void tagAndUntagMultiplePersonsAccelerator() {
        clickOnMultiplePersons(td.alice, td.benson, td.charlie);
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
    public void tagAndUntagMultiplePersonsAccelerator_multipleTags() {
        clickOnMultiplePersons(td.alice, td.benson, td.charlie);
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

    private void searchAndAcceptTags(TagPersonDialogHandle aliceTagDialog, String... tagQueries) {
        for (String tagQuery : tagQueries) {
            aliceTagDialog.searchAndAcceptTag(tagQuery);
        }
    }

    private void clickOnMultiplePersons(Person... persons) {
        List<Person> personList = Arrays.asList(persons);
        personListPanel.clickOnMultiplePersons(personList);
        personList.forEach(person -> {
            assertTrue(personListPanel.getSelectedCards().contains(personListPanel.getPersonCardHandle(person)));
        });
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
