package guitests;

import address.model.datatypes.person.Person;
import guitests.guihandles.PersonCardHandle;
import guitests.guihandles.TagPersonDialogHandle;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TagPersonHeadfulGuiTest extends GuiTestBase {
    @Test
    public void tagMultiplePersonsAccelerator() {
        clickOnMultiplePersons(td.alice, td.benson, td.charlie);

        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandle.searchAndAcceptTags("frie");
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
        multiplePersonsTagDialogHandle.searchAndAcceptTags("frie", "coll");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        waitForGracePeriod("Tag: friends, Tag: colleagues", alicePersonCard, bensonPersonCard, charliePersonCard);
    }

    @Test
    public void tagMultiplePersonsAccelerator_multipleTagsAndCancelDuringGracePeriod() {
        clickOnMultiplePersons(td.alice, td.benson, td.charlie);
        TagPersonDialogHandle multiplePersonsTagDialogHandle = personListPanel.use_PERSON_TAG_ACCELERATOR();
        multiplePersonsTagDialogHandle.searchAndAcceptTags("frie", "coll");
        multiplePersonsTagDialogHandle.pressEnter();

        PersonCardHandle alicePersonCard = personListPanel.getPersonCardHandle(td.alice);
        PersonCardHandle bensonPersonCard = personListPanel.getPersonCardHandle(td.benson);
        PersonCardHandle charliePersonCard = personListPanel.getPersonCardHandle(td.charlie);

        cancelDuringGracePeriod("Tag: friends, Tag: colleagues", "", alicePersonCard, bensonPersonCard, charliePersonCard);
    }

    @Test
    public void tagAndUntagMultiplePersonsAccelerator() {
        clickOnMultiplePersons(td.alice, td.benson, td.charlie);
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
    public void tagAndUntagMultiplePersonsAccelerator_multipleTags() {
        clickOnMultiplePersons(td.alice, td.benson, td.charlie);
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

    //TODO: The following helper methods are copied from TagPersonGuiTest - extract them into util?
    private void assertSelectedCardHandles(PersonCardHandle... personCardHandles) {
        for (PersonCardHandle personCardHandle : personCardHandles) {
            assertTrue(personListPanel.getSelectedCards().contains(personCardHandle));
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
}
