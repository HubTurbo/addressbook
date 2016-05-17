package address.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import address.events.EventManager;
import address.events.GroupSearchResultsChangedEvent;
import address.events.GroupsChangedEvent;

public class PersonEditDialogGroupsModel {
    List<SelectableContactGroup> groups = new ArrayList<>();
    List<SelectableContactGroup> filteredGroups = new ArrayList<>();
    Optional<Integer> selectedGroupIndex = Optional.empty();

    List<SelectableContactGroup> assignedGroups = new ArrayList<>();

    public PersonEditDialogGroupsModel(List<ContactGroup> groups, List<ContactGroup> assignedGroups) {
        List<SelectableContactGroup> selectableContactGroups = groups.stream()
                                                                .map(SelectableContactGroup::new)
                                                                .collect(Collectors.toList());
        this.groups.addAll(selectableContactGroups);

        assignedGroups.stream()
                .forEach(assignedGroup -> this.assignedGroups.addAll(this.groups.stream()
                        .filter(group -> group.getName().equals(assignedGroup.getName()))
                        .collect(Collectors.toList())));

        EventManager.getInstance().post(new GroupsChangedEvent(this.assignedGroups));
        setFilter("");
    }

    private Optional<SelectableContactGroup> getSelection() {
        return filteredGroups.stream()
                .filter(SelectableContactGroup::isSelected)
                .findFirst();
    }

    public void toggleSelection() {
        Optional<SelectableContactGroup> selection = getSelection();
        if (!selection.isPresent()) return;

        if (assignedGroups.contains(selection.get())) {
            assignedGroups.remove(selection.get());
        } else {
            assignedGroups.add(selection.get());
        }

        EventManager.getInstance().post(new GroupsChangedEvent(assignedGroups));
    }

    private void selectIndex(int index) {
        clearSelection();
        SelectableContactGroup group = filteredGroups.remove(index);
        group.setSelected(true);
        filteredGroups.add(index, group);
    }

    private void clearSelection() {
        for (int i = 0; i < filteredGroups.size(); i++) {
            if (filteredGroups.get(i).isSelected()) {
                SelectableContactGroup group = filteredGroups.remove(i);
                group.setSelected(false);
                filteredGroups.add(i, group);
            }
        }
    }

    public void selectNext() {
        if (!canIncreaseIndex()) return;
        selectedGroupIndex = Optional.of(selectedGroupIndex.orElse(-1) + 1);
        updateSelection();
        EventManager.getInstance().post(new GroupSearchResultsChangedEvent(filteredGroups));
    }

    private void updateSelection() {
        if (selectedGroupIndex.isPresent()) {
            selectIndex(selectedGroupIndex.get());
        } else {
            clearSelection();
        }
    }

    private boolean canIncreaseIndex() {
        return !filteredGroups.isEmpty()
                && (!selectedGroupIndex.isPresent() || selectedGroupIndex.get() < filteredGroups.size() - 1);
    }

    public void selectPrevious() {
        if (!canDecreaseIndex()) return;
        selectedGroupIndex = Optional.of(selectedGroupIndex.get() - 1);
        updateSelection();
        EventManager.getInstance().post(new GroupSearchResultsChangedEvent(filteredGroups));
    }

    private boolean canDecreaseIndex() {
        return selectedGroupIndex.isPresent() && selectedGroupIndex.get() > 0;
    }

    public void setFilter(String filter) {

        List<SelectableContactGroup> newContactGroups = groups.stream()
                .filter(group -> group.getName().contains(filter))
                .collect(Collectors.toList());

        List<SelectableContactGroup> toBeAdded = newContactGroups.stream()
                .filter(newContactGroup -> !filteredGroups.contains(newContactGroup))
                .collect(Collectors.toList());
        List<SelectableContactGroup> toBeRemoved = filteredGroups.stream()
                .filter(oldContactGroup -> !newContactGroups.contains(oldContactGroup))
                .collect(Collectors.toList());

        toBeRemoved.stream()
                .forEach(toRemove -> filteredGroups.remove(toRemove));
        toBeAdded.stream()
                .forEach(toAdd -> filteredGroups.add(toAdd));

        if (!filter.isEmpty() && !filteredGroups.isEmpty()) {
            selectedGroupIndex = Optional.of(0);
        } else {
            selectedGroupIndex = Optional.empty();
        }
        updateSelection();

        EventManager.getInstance().post(new GroupSearchResultsChangedEvent(filteredGroups));
    }

    public List<ContactGroup> getAssignedGroups() {
        return assignedGroups.stream()
                .map(assignedGroup -> new ContactGroup(assignedGroup.getName()))
                .collect(Collectors.toList());
    }
}
