package guitests;

import address.model.datatypes.AddressBook;
import address.model.datatypes.person.Person;
import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.PersonCardHandle;
import guitests.guihandles.TagPersonDialogHandle;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

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
    }

    @Test
    public void tagPersonThroughPersonEditDialog_alreadyHasOtherTags() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("frien");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();
    }

    @Test
    public void tagPersonThroughPersonEditDialog_cancelDuringPendingState() {
        PersonCardHandle charliePersonCard = personListPanel.selectCard(td.charlie);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();
    }

    @Test
    public void tagPersonThroughPersonEditDialog_multipleTags() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll", "frie", "fam");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();
    }

    @Test
    public void tagPersonThroughPersonEditDialog_multipleTagsAndCancelDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll", "frie");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();
    }

    @Test
    public void tagPersonThroughPersonEditDialog_severalTagSelectionChanges() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll", "frie", "coll", "fam", "coll", "fam", "coll");
        aliceTagDialog.close();

        aliceEditDialog.pressEnter();
    }

    @Test
    public void tagAndUntagPersonThroughPersonEditDialog() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialog();

        aliceTagDialogTwo.searchAndAcceptTags("coll");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();
    }

    @Test
    public void tagAndChangeTagThroughPersonEditDialog() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll", "frie");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();
    }

    @Test
    public void tagPersonThroughPersonEditDialog_changeTagDuringPendingState() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        EditPersonDialogHandle aliceEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialog = aliceEditDialog.openTagPersonDialog();

        aliceTagDialog.searchAndAcceptTags("coll");

        aliceTagDialog.close();
        aliceEditDialog.pressEnter();

        EditPersonDialogHandle aliceEditDialogTwo = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle aliceTagDialogTwo = aliceEditDialogTwo.openTagPersonDialog();

        aliceTagDialogTwo.searchAndAcceptTags("coll", "frie");

        aliceTagDialogTwo.close();
        aliceEditDialogTwo.pressEnter();
    }

    @Test
    public void tagPersonAccelerator_multipleTags() {
        PersonCardHandle alicePersonCard = personListPanel.selectCard(td.alice);
        TagPersonDialogHandle tagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        tagDialogHandle.searchAndAcceptTags("frie", "coll");
        tagDialogHandle.pressEnter();
    }

    @Test
    public void tagPersonAccelerator() {
        PersonCardHandle bensonPersonCard = personListPanel.selectCard(td.benson);
        TagPersonDialogHandle tagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        tagDialogHandle.searchAndAcceptTags("frie");
        tagDialogHandle.pressEnter();
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
        assertSelectedCardHandles(alicePersonCard, bensonPersonCard, charliePersonCard);
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

        assertSelectedCardHandles(alicePersonCard, bensonPersonCard, charliePersonCard);
        TagPersonDialogHandle multiplePersonsTagDialogHandleTwo = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandleTwo.searchAndAcceptTags("frie");
        multiplePersonsTagDialogHandleTwo.pressEnter();
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

        assertSelectedCardHandles(alicePersonCard, bensonPersonCard, charliePersonCard);
        TagPersonDialogHandle multiplePersonsTagDialogHandleTwo = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandleTwo.searchAndAcceptTags("frie");
        multiplePersonsTagDialogHandleTwo.pressEnter();
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

        assertSelectedCardHandles(alicePersonCard, bensonPersonCard, charliePersonCard);
        TagPersonDialogHandle multiplePersonsTagDialogHandleTwo = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandleTwo.searchAndAcceptTags("frie");
        multiplePersonsTagDialogHandleTwo.pressEnter();
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

    private void assertSelectedCardHandles(PersonCardHandle... personCardHandles) {
        for (PersonCardHandle personCardHandle : personCardHandles) {
            assertTrue(personListPanel.getSelectedCards().contains(personCardHandle));
        }
    }
}
