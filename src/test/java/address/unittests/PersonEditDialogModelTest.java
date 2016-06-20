package address.unittests;

import address.events.EventManager;
import address.events.TagSearchResultsChangedEvent;
import address.model.datatypes.tag.Tag;
import address.model.TagSelectionEditDialogModel;
import com.google.common.eventbus.Subscribe;
import org.junit.Before;
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

    int eventCounter;
    ArrayList<Tag> eventData;

    @Subscribe
    public void handleTagSearchResultsChangedEvent(TagSearchResultsChangedEvent e) {
        eventCounter++;
        eventData.clear();
        eventData.addAll(e.getSelectableTags());
    }

    @Before
    public void setup() {
        EventManager.getInstance().registerHandler(this);
        eventCounter = 0;
        eventData = new ArrayList<>();
    }

    @Test
    public void filterTags() {
        List<Tag> allTags = getList("friends", "relatives", "colleagues");
        List<Tag> assignedTags = getList("friends");
        TagSelectionEditDialogModel model = new TagSelectionEditDialogModel(allTags, assignedTags);
        model.setFilter("ela");

        assertEquals(2, eventCounter);
        assertEquals(1, eventData.size());
        assertEquals(allTags.get(1), eventData.get(0));
    }
}
