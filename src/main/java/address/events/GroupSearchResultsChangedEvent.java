package address.events;

import address.model.SelectableContactGroup;

import java.util.List;

public class GroupSearchResultsChangedEvent extends BaseEvent {
    List<SelectableContactGroup> selectableContactGroups;

    public GroupSearchResultsChangedEvent(List<SelectableContactGroup> selectableContactGroups) {
        this.selectableContactGroups = selectableContactGroups;
    }

    public List<SelectableContactGroup> getSelectableContactGroups() {
        return this.selectableContactGroups;
    }

    @Override
    public String toString() {
        return "Group search results have changed: now " + selectableContactGroups.size() + " items.";
    }
}
