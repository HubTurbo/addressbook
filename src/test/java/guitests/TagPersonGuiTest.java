package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.PersonCardHandle;
import guitests.guihandles.TagPersonDialogHandle;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TagPersonGuiTest extends GuiTestBase {
    @Override
    protected AddressBook getInitialData() {
        verifyTestData();
        return td.book;
    }

    private void verifyTestData() {
        // check that initial data is intact
        assertEquals(1, td.alice.getTags().size());
        assertEquals("friends", td.alice.getTags().get(0).getName());

        assertEquals(0, td.benson.getTags().size());
        assertEquals(0, td.charlie.getTags().size());
    }

    @Test
    public void tagPersonThroughPersonEditDialog() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard);
    }

    @Test
    public void tagPersonThroughPersonEditDialog_alreadyHasOtherTags() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("frien");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("", alicePersonCard);
    }

    @Test
    public void tagPersonThroughPersonEditDialog_cancelDuringPendingState() {
        PersonCardHandle charliePersonCard = personListPanel.selectCard(td.charlie);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        cancelDuringGracePeriod("Tag: colleagues", "", charliePersonCard);
    }

    @Test
    public void tagPersonThroughPersonEditDialog_multipleTags() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll", "frie", "fam");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: colleagues, Tag: family", alicePersonCard);
    }

    @Test
    public void tagPersonThroughPersonEditDialog_multipleTagsAndCancelDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll", "frie");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        cancelDuringGracePeriod("Tag: colleagues", "Tag: friends", alicePersonCard);
    }

    @Test
    public void tagPersonThroughPersonEditDialog_severalTagSelectionChanges() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll", "frie", "coll", "fam", "coll", "fam", "coll");
        aliceTagDialog.close();

        aliceEditDialog.pressEnter();

        waitForGracePeriod("", alicePersonCard);
    }

    @Test
    public void tagAndUntagPersonThroughPersonEditDialog() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard);

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialog();

        aliceTagDialogTwo.searchAndAcceptTags("coll");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        waitForGracePeriod("Tag: friends", alicePersonCard);
    }

    @Test
    public void tagAndChangeTagThroughPersonEditDialog() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard);

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll", "frie");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        waitForGracePeriod("", alicePersonCard);
    }

    @Test
    public void tagAndChangeTagThroughPersonEditDialog_cancelDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard);

        // Perform another edit before grace period ends
        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialog();

        aliceTagDialogTwo.searchAndAcceptTags("coll", "frie");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        cancelDuringGracePeriod("", "Tag: friends, Tag: colleagues", alicePersonCard);
    }

    @Test
    public void tagPersonThroughPersonEditDialog_changeTagDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        assertTagsBeforeGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard);

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialog();

        aliceTagDialogTwo.searchAndAcceptTags("coll", "frie");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();

        cancelDuringGracePeriod("", "Tag: friends", alicePersonCard);
    }

    @Test
    public void tagPersonAccelerator_multipleTags() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        TagPersonDialogHandle tagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        tagDialogHandle.searchAndAcceptTags("frie", "coll");
        tagDialogHandle.pressEnter();

        waitForGracePeriod("Tag: colleagues", alicePersonCard);
    }

    @Test
    public void tagPersonAccelerator_multipleTagsAndCancelDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        TagPersonDialogHandle tagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        tagDialogHandle.searchAndAcceptTags("frie", "coll");
        tagDialogHandle.pressEnter();

        cancelDuringGracePeriod("Tag: colleagues", "Tag: friends", alicePersonCard);
    }

    @Test
    public void tagPersonAccelerator() {
        PersonCardHandle bensonPersonCard = personListPanel.selectCard(td.benson);
        TagPersonDialogHandle tagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        tagDialogHandle.searchAndAcceptTags("frie");
        tagDialogHandle.pressEnter();

        waitForGracePeriod("Tag: friends", bensonPersonCard);
    }

    @Test
    public void tagPersonAccelerator_noSelectedPerson() {
        personListPanel.use_PERSON_TAG_ACCELERATOR();
        assertTrue(personListPanel.isNoSelectedPersonDialogShown());
    }
    
    @Test
    public void tagMultiplePersonsAccelerator() {
        selectMultiplePersons(td.alice, td.benson, td.charlie);

        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandle.searchAndAcceptTags("frie");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        waitForGracePeriod("Tag: friends", alicePersonCard, bensonPersonCard, charliePersonCard);
    }

    @Test
    public void tagMultiplePersonsAccelerator_cancelDuringGracePeriod() {
        selectMultiplePersons(td.alice, td.benson, td.charlie);

        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandle.searchAndAcceptTags("frie");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        assertTagsBeforeGracePeriod("Tag: friends", alicePersonCard, bensonPersonCard, charliePersonCard);
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertTagsAfterGracePeriod("Tag: friends", alicePersonCard);
        assertTagsAfterGracePeriod("", bensonPersonCard, charliePersonCard);
    }

    @Test
    public void tagMultiplePersonsAccelerator_multipleTags() {
        selectMultiplePersons(td.alice, td.benson, td.charlie);
        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandle.searchAndAcceptTags("frie", "coll");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard, bensonPersonCard, charliePersonCard);
    }

    @Test
    public void tagMultiplePersonsAccelerator_multipleTagsAndCancelDuringGracePeriod() {
        selectMultiplePersons(td.alice, td.benson, td.charlie);
        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandle.searchAndAcceptTags("frie", "coll");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        assertTagsBeforeGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard, bensonPersonCard, charliePersonCard);
        personListPanel.use_PERSON_CHANGE_CANCEL_ACCELERATOR();
        assertTagsAfterGracePeriod("Tag: friends");
        assertTagsAfterGracePeriod("", bensonPersonCard, charliePersonCard);
    }

    @Test
    public void tagAndUntagMultiplePersonsAccelerator() {
        selectMultiplePersons(td.alice, td.benson, td.charlie);
        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandle.searchAndAcceptTags("frie");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        waitForGracePeriod("Tag: friends", alicePersonCard, bensonPersonCard, charliePersonCard);

        assertSelectedCardHandles(alicePersonCard, bensonPersonCard, charliePersonCard);
        TagPersonDialogHandle multiplePersonsTagDialogHandleTwo = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandleTwo.searchAndAcceptTags("frie");
        multiplePersonsTagDialogHandleTwo.pressEnter();

        waitForGracePeriod("", alicePersonCard, bensonPersonCard, charliePersonCard);
    }

    @Test
    public void tagAndUntagMultiplePersonsAccelerator_cancelDuringGracePeriod() {
        selectMultiplePersons(td.alice, td.benson, td.charlie);
        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandle.searchAndAcceptTags("frie");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        waitForGracePeriod("Tag: friends", alicePersonCard, bensonPersonCard, charliePersonCard);

        assertSelectedCardHandles(alicePersonCard, bensonPersonCard, charliePersonCard);
        TagPersonDialogHandle multiplePersonsTagDialogHandleTwo = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandleTwo.searchAndAcceptTags("frie");
        multiplePersonsTagDialogHandleTwo.pressEnter();

        cancelDuringGracePeriod("", "Tag: friends", alicePersonCard, bensonPersonCard, charliePersonCard);
    }

    @Test
    public void tagAndUntagMultiplePersonsAccelerator_multipleTags() {
        selectMultiplePersons(td.alice, td.benson, td.charlie);
        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandle.searchAndAcceptTags("frie", "coll");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard, bensonPersonCard, charliePersonCard);

        assertSelectedCardHandles(alicePersonCard, bensonPersonCard, charliePersonCard);
        TagPersonDialogHandle multiplePersonsTagDialogHandleTwo = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandleTwo.searchAndAcceptTags("frie");
        multiplePersonsTagDialogHandleTwo.pressEnter();

        waitForGracePeriod("Tag: colleagues", alicePersonCard, bensonPersonCard, charliePersonCard);
    }

    @Test
    public void tagAndUntagMultiplePersonsAccelerator_multipleTagsAndCancelDuringGracePeriod() {
        selectMultiplePersons(td.alice, td.benson, td.charlie);
        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandle.searchAndAcceptTags("frie", "coll");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard, bensonPersonCard, charliePersonCard);

        assertSelectedCardHandles(alicePersonCard, bensonPersonCard, charliePersonCard);
        TagPersonDialogHandle multiplePersonsTagDialogHandleTwo = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandleTwo.searchAndAcceptTags("frie");
        multiplePersonsTagDialogHandleTwo.pressEnter();

        cancelDuringGracePeriod("Tag: colleagues", "Tag: friends, Tag: colleagues", alicePersonCard, bensonPersonCard,
                charliePersonCard);
    }

    /**
     * Attempts to select the cards of the given persons
     * @param persons
     */
    private void selectMultiplePersons(Person... persons) {
        List<Person> personList = Arrays.asList(persons);
        personListPanel.selectMultiplePersons(personList);

        List<PersonCardHandle> personCardHandleList = personList.stream()
                .map(personListPanel::getPersonCardHandle)
                .collect(Collectors.toCollection(ArrayList::new));
        assertTrue(personListPanel.getSelectedCards().containsAll(personCardHandleList));
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
