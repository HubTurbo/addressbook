package address.model;

import address.model.datatypes.tag.SelectableTag;
import address.model.datatypes.tag.Tag;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TagSelectionEditDialogModel {
    private List<Tag> allTags;
    private ObservableList<SelectableTag> filteredTags;
    private Optional<Integer> selectedTagIndex;
    private ObservableList<Tag> assignedTags;

    public TagSelectionEditDialogModel() {
        allTags = FXCollections.observableArrayList();
        filteredTags = FXCollections.observableArrayList();
        selectedTagIndex = Optional.empty();
        assignedTags = FXCollections.observableArrayList();
    }

    public void init(List<Tag> allTags, List<Tag> assignedTags, String filter) {
        this.allTags.addAll(allTags);
        this.assignedTags.addAll(assignedTags);
        setFilter(filter);
    }

    public void toggleSelection() {
        Optional<SelectableTag> selection = getSelection(filteredTags);
        if (!selection.isPresent()) return;

        SelectableTag selectedTag = selection.get();
        boolean isRemoved = assignedTags.removeIf(tag -> tag.equals(selectedTag));
        if (!isRemoved) assignedTags.add(selectedTag);
    }

    public void selectNext() {
        if (!canIncreaseIndex()) return;
        selectedTagIndex = Optional.of(selectedTagIndex.orElse(-1) + 1);
        updateSelection(filteredTags, selectedTagIndex);
    }

    public void selectPrevious() {
        if (!canDecreaseIndex()) return;
        selectedTagIndex = Optional.of(selectedTagIndex.get() - 1);
        updateSelection(filteredTags, selectedTagIndex);
    }

    public void setFilter(String filter) {
        filteredTags.setAll(convertToSelectableTags(getMatchingTags(allTags, filter)));
        selectedTagIndex = getSelectedTagIndexAfterFilter(filteredTags, filter);
        updateSelection(filteredTags, selectedTagIndex);
    }

    private List<Tag> getMatchingTags(List<Tag> tagList, String filter) {
        return tagList.stream()
                .filter(tag -> tag.getName().contains(filter))
                .collect(Collectors.toList());
    }

    private List<SelectableTag> convertToSelectableTags(List<Tag> tagList) {
        return tagList.stream()
                .map(SelectableTag::new)
                .collect(Collectors.toList());
    }

    public ObservableList<Tag> getAssignedTags() {
        return assignedTags;
    }

    public ObservableList<SelectableTag> getFilteredTags() {
        return filteredTags;
    }

    private Optional<SelectableTag> getSelection(List<SelectableTag> tagList) {
        return tagList.stream()
                .filter(SelectableTag::isSelected)
                .findFirst();
    }

    private void updateSelection(List<SelectableTag> tagList, Optional<Integer> tagIndex) {
        clearSelection(tagList);
        if (!tagIndex.isPresent()) return;
        int tagIndexToSelect = tagIndex.get();
        // remove then add back to notify change listener
        SelectableTag tag = tagList.remove(tagIndexToSelect);
        tag.setSelected(true);
        tagList.add(tagIndexToSelect, tag);
    }

    private void clearSelection(List<SelectableTag> tagList) {
        tagList.stream()
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

    private Optional<Integer> getSelectedTagIndexAfterFilter(List<SelectableTag> tagList, String filter) {
        if (!filter.isEmpty() && !tagList.isEmpty()) {
            return Optional.of(0);
        } else {
            return Optional.empty();
        }
    }
}
