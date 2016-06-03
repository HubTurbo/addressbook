package address.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import address.events.EventManager;
import address.events.TagSearchResultsChangedEvent;
import address.events.TagsChangedEvent;
import address.model.datatypes.SelectableTag;
import address.model.datatypes.Tag;

public class PersonEditDialogTagsModel {
    List<SelectableTag> tags = new ArrayList<>();
    List<SelectableTag> filteredTags = new ArrayList<>();
    Optional<Integer> selectedTagIndex = Optional.empty();

    List<SelectableTag> assignedTags = new ArrayList<>();

    public PersonEditDialogTagsModel(List<Tag> tags, List<Tag> assignedTags) {
        List<SelectableTag> selectableTargets = tags.stream()
                                                    .map(SelectableTag::new)
                                                    .collect(Collectors.toList());
        this.tags.addAll(selectableTargets);

        assignedTags.stream()
                .forEach(assignedTag -> this.assignedTags.addAll(this.tags.stream()
                        .filter(tag -> tag.getName().equals(assignedTag.getName()))
                        .collect(Collectors.toList())));

        EventManager.getInstance().post(new TagsChangedEvent(this.assignedTags));
        setFilter("");
    }

    private Optional<SelectableTag> getSelection() {
        return filteredTags.stream()
                .filter(SelectableTag::isSelected)
                .findFirst();
    }

    public void toggleSelection() {
        Optional<SelectableTag> selection = getSelection();
        if (!selection.isPresent()) return;

        if (assignedTags.contains(selection.get())) {
            assignedTags.remove(selection.get());
        } else {
            assignedTags.add(selection.get());
        }

        EventManager.getInstance().post(new TagsChangedEvent(assignedTags));
    }

    private void selectIndex(int index) {
        clearSelection();
        SelectableTag tag = filteredTags.remove(index);
        tag.setSelected(true);
        filteredTags.add(index, tag);
    }

    private void clearSelection() {
        for (int i = 0; i < filteredTags.size(); i++) {
            if (filteredTags.get(i).isSelected()) {
                SelectableTag tag = filteredTags.remove(i);
                tag.setSelected(false);
                filteredTags.add(i, tag);
            }
        }
    }

    public void selectNext() {
        if (!canIncreaseIndex()) return;
        selectedTagIndex = Optional.of(selectedTagIndex.orElse(-1) + 1);
        updateSelection();
        EventManager.getInstance().post(new TagSearchResultsChangedEvent(filteredTags));
    }

    private void updateSelection() {
        if (selectedTagIndex.isPresent()) {
            selectIndex(selectedTagIndex.get());
        } else {
            clearSelection();
        }
    }

    private boolean canIncreaseIndex() {
        return !filteredTags.isEmpty()
                && (!selectedTagIndex.isPresent() || selectedTagIndex.get() < filteredTags.size() - 1);
    }

    public void selectPrevious() {
        if (!canDecreaseIndex()) return;
        selectedTagIndex = Optional.of(selectedTagIndex.get() - 1);
        updateSelection();
        EventManager.getInstance().post(new TagSearchResultsChangedEvent(filteredTags));
    }

    private boolean canDecreaseIndex() {
        return selectedTagIndex.isPresent() && selectedTagIndex.get() > 0;
    }

    public void setFilter(String filter) {

        List<SelectableTag> newContactTags = tags.stream()
                .filter(tag -> tag.getName().contains(filter))
                .collect(Collectors.toList());

        List<SelectableTag> toBeAdded = newContactTags.stream()
                .filter(newContactTag -> !filteredTags.contains(newContactTag))
                .collect(Collectors.toList());
        List<SelectableTag> toBeRemoved = filteredTags.stream()
                .filter(oldContactTag -> !newContactTags.contains(oldContactTag))
                .collect(Collectors.toList());

        toBeRemoved.stream()
                .forEach(toRemove -> filteredTags.remove(toRemove));
        toBeAdded.stream()
                .forEach(toAdd -> filteredTags.add(toAdd));

        if (!filter.isEmpty() && !filteredTags.isEmpty()) {
            selectedTagIndex = Optional.of(0);
        } else {
            selectedTagIndex = Optional.empty();
        }
        updateSelection();

        EventManager.getInstance().post(new TagSearchResultsChangedEvent(filteredTags));
    }

    public List<Tag> getAssignedTagss() {
        return assignedTags.stream()
                .map(assignedTag -> new Tag(assignedTag.getName()))
                .collect(Collectors.toList());
    }
}
