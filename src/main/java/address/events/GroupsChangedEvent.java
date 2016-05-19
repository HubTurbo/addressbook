package address.events;

import address.model.SelectableContactGroup;

import java.util.List;

public class GroupsChangedEvent extends BaseEvent {
    List<SelectableContactGroup> resultGroup;

    public GroupsChangedEvent(List<SelectableContactGroup> resultGroup) {
        this.resultGroup = resultGroup;
    }

    public List<SelectableContactGroup> getResultGroup() {
        return this.resultGroup;
    }

    @Override
    public String toString() {
        return "Groups selection has changed, now " + resultGroup.size() + " items.";
    }
}
