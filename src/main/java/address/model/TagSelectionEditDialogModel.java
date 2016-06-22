package address.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import address.events.EventManager;
import address.events.TagSelectionSearchResultsChangedEvent;
import address.events.TagSelectionListChangedEvent;
import address.model.datatypes.tag.SelectableTag;
import address.model.datatypes.tag.Tag;

public class TagSelectionEditDialogModel {
    private List<SelectableTag> allTags = new ArrayList<>();
    private List<SelectableTag> filteredTags = new ArrayList<>();
    private Optional<Integer> selectedTagIndex = Optional.empty();
    private List<SelectableTag> assignedTags = new ArrayList<>();

    public TagSelectionEditDialogModel(List<Tag> allTags, List<Tag> assignedTags) {
        List<SelectableTag> selectableTags = convertToSelectableTags(allTags);
        this.allTags.addAll(selectableTags);

        assignedTags.stream()
                .forEach(assignedTag -> this.assignedTags.add(getSelectableTag(assignedTag).get()));

        EventManager.getInstance().post(new TagSelectionListChangedEvent(this.assignedTags));
        setFilter("");
    }

    public void toggleSelection() {
        Optional<SelectableTag> selection = getSelection();
        if (!selection.isPresent()) return;

        if (assignedTags.contains(selection.get())) {
            assignedTags.remove(selection.get());
        } else {
            assignedTags.add(selection.get());
        }

        EventManager.getInstance().post(new TagSelectionListChangedEvent(assignedTags));
    }

    public void selectNext() {
        if (!canIncreaseIndex()) return;
        selectedTagIndex = Optional.of(selectedTagIndex.orElse(-1) + 1);
        updateSelection();
        EventManager.getInstance().post(new TagSelectionSearchResultsChangedEvent(filteredTags));
    }

    public void selectPrevious() {
        if (!canDecreaseIndex()) return;
        selectedTagIndex = Optional.of(selectedTagIndex.get() - 1);
        updateSelection();
        EventManager.getInstance().post(new TagSelectionSearchResultsChangedEvent(filteredTags));
    }

    public void setFilter(String filter) {
        List<SelectableTag> newContactTags = allTags.stream()
                                                .filter(tag -> tag.getName().contains(filter))
                                                .collect(Collectors.toList());

        List<SelectableTag> toBeAdded = getNewMatchingTags(filteredTags, newContactTags);
        List<SelectableTag> toBeRemoved = getNewMatchingTags(newContactTags, filteredTags);

        filteredTags.removeAll(toBeRemoved);
        filteredTags.addAll(toBeAdded);

        updateSelectedTagIndexAfterFilter(filter);
        updateSelection();

        EventManager.getInstance().post(new TagSelectionSearchResultsChangedEvent(filteredTags));
    }

    public List<Tag> getAssignedTags() {
        return assignedTags.stream()
                .map(assignedTag -> new Tag(assignedTag.getName()))
                .collect(Collectors.toList());
    }

    private List<SelectableTag> convertToSelectableTags(List<Tag> allTags) {
        return allTags.stream()
                .map(SelectableTag::new)
                .collect(Collectors.toList());
    }

    private Optional<SelectableTag> getSelectableTag(Tag assignedTag) {
        return this.allTags.stream()
                .filter(tag -> tag.getName().equals(assignedTag.getName()))
                .findFirst();
    }

    private Optional<SelectableTag> getSelection() {
        return filteredTags.stream()
                .filter(SelectableTag::isSelected)
                .findFirst();
    }

    private void updateSelection() {
        clearSelection();
        if (!selectedTagIndex.isPresent()) return;
        int tagIndexToSelect = selectedTagIndex.get();
        SelectableTag tag = filteredTags.remove(tagIndexToSelect);
        tag.setSelected(true);
        filteredTags.add(tagIndexToSelect, tag);
    }

    private void clearSelection() {
        filteredTags.stream()
                .filter(SelectableTag::isSelected)
                .forEach(tag -> tag.setSelected(false));
    }

    private boolean canIncreaseIndex() {
        return !filteredTags.isEmpty()
                && (!selectedTagIndex.isPresent() || selectedTagIndex.get() < filteredTags.size() - 1);
    }

    private boolean canDecreaseIndex() {
        return selectedTagIndex.isPresent() && selectedTagIndex.get() > 0;
    }

    private void updateSelectedTagIndexAfterFilter(String filter) {
        if (!filter.isEmpty() && !filteredTags.isEmpty()) {
            selectedTagIndex = Optional.of(0);
        } else {
            selectedTagIndex = Optional.empty();
        }
    }

    /**
     * Gets the list of tags that appears in newContactTags but not in originalTags
     */
    private List<SelectableTag> getNewMatchingTags(List<SelectableTag> originalTags, List<SelectableTag> updatedTags) {
        return updatedTags.stream()
                .filter(newContactTag -> !originalTags.contains(newContactTag))
                .collect(Collectors.toList());
    }
}
