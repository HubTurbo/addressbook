package address.model;

import address.model.datatypes.tag.Tag;
import address.model.TagSelectionEditDialogModel;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PersonEditDialogModelTest {
    private static List<Tag> getList(String... tags) {
        List<Tag> tagList = new ArrayList<>();
        for (String tag : tags) {
            tagList.add(new Tag(tag));
        }

        return tagList;
    }

    @Test
    public void filterTags() {
        List<Tag> allTags = getList("friends", "relatives", "colleagues");
        List<Tag> assignedTags = getList("friends");
        TagSelectionEditDialogModel model = new TagSelectionEditDialogModel();
        model.init(allTags, assignedTags, "");
        model.setFilter("ela");

        assertEquals(allTags.get(1), model.getFilteredTags().get(0));
    }
}
