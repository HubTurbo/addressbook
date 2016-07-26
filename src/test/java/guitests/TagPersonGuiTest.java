package guitests;

import guitests.guihandles.EditPersonDialogHandle;
import guitests.guihandles.PersonCardHandle;
import guitests.guihandles.TagPersonDialogHandle;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TagPersonGuiTest extends GuiTestBase {
    @Test
    public void tagPerson() {
        PersonCardHandle bensonPersonCard = personListPanel.selectCard(td.benson);
        EditPersonDialogHandle bensonEditDialog = personListPanel.use_PERSON_EDIT_ACCELERATOR();
        TagPersonDialogHandle bensonTagDialog = bensonEditDialog.openTagPersonDialogUsingShortcut();

        bensonTagDialog.enterSearchQuery("coll").acceptSuggestedTag();
        bensonTagDialog.close();

        bensonEditDialog.pressEnter();

        assertEquals("Tag: colleagues", bensonPersonCard.getTags());
        assertEquals("Editing", bensonPersonCard.getPendingStateLabel());
        sleepForGracePeriod();
        assertEquals("Tag: colleagues", bensonPersonCard.getTags());
    }
}
