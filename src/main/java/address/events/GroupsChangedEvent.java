package address.events;

import address.controller.PersonEditDialogController.SelectableContactGroup;

import java.util.List;

public class GroupsChangedEvent {
    List<SelectableContactGroup> resultGroup;

    public GroupsChangedEvent(List<SelectableContactGroup> resultGroup) {
        this.resultGroup = resultGroup;
    }

    public List<SelectableContactGroup> getResultGroup() {
        return this.resultGroup;
    }
}
