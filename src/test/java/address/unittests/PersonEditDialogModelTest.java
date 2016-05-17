package address.unittests;

import address.events.EventManager;
import address.events.GroupSearchResultsChangedEvent;
import address.model.ContactGroup;
import address.model.PersonEditDialogGroupsModel;
import com.google.common.eventbus.Subscribe;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PersonEditDialogModelTest {
    private static List<ContactGroup> getList(String... groups) {
        List<ContactGroup> groupList = new ArrayList<>();
        for (String group : groups) {
            groupList.add(new ContactGroup(group));
        }

        return groupList;
    }

    int eventCounter;
    ArrayList<ContactGroup> eventData;

    @Subscribe
    public void handleGroupSearchResultsChangedEvent(GroupSearchResultsChangedEvent e) {
        eventCounter++;
        eventData.clear();
        eventData.addAll(e.getSelectableContactGroups());
    }

    @Before
    public void setup() {
        EventManager.getInstance().registerHandler(this);
        eventCounter = 0;
        eventData = new ArrayList<>();
    }

    @Test
    public void filterGroups() {
        List<ContactGroup> allGroups = getList("friends", "relatives", "colleagues");
        List<ContactGroup> assignedGroups = getList("friends");
        PersonEditDialogGroupsModel model = new PersonEditDialogGroupsModel(allGroups, assignedGroups);
        model.setFilter("ela");

        assertEquals(2, eventCounter);
        assertEquals(1, eventData.size());
        assertEquals(allGroups.get(1), eventData.get(0));
    }
}
