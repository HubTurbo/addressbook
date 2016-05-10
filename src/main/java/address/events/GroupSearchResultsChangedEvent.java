package address.events;

import address.controller.PersonEditDialogController.SelectableContactGroup;

import java.util.List;

public class GroupSearchResultsChangedEvent {
    List<SelectableContactGroup> selectableContactGroups;

    public GroupSearchResultsChangedEvent(List<SelectableContactGroup> selectableContactGroups) {
        this.selectableContactGroups = selectableContactGroups;
    }

    public List<SelectableContactGroup> getSelectableContactGroups() {
        return this.selectableContactGroups;
    }

}
