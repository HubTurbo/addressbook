package address.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import address.model.datatypes.tag.SelectableTag;
import address.model.datatypes.tag.Tag;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TagSelectionEditDialogModel {
    private List<Tag> allTags = FXCollections.observableArrayList();
    private ObservableList<SelectableTag> filteredTags = FXCollections.observableArrayList();
    private Optional<Integer> selectedTagIndex = Optional.empty();
    private ObservableList<Tag> assignedTags = FXCollections.observableArrayList();

    public TagSelectionEditDialogModel() {
    }

    public void initModel(List<Tag> allTags, List<Tag> assignedTags, String filter) {
        this.allTags.addAll(allTags);
        this.assignedTags.addAll(assignedTags);
        setFilter(filter);
    }

    public void toggleSelection() {
        Optional<SelectableTag> selection = getSelection();
        if (!selection.isPresent()) return;

        if (assignedTags.contains(selection.get())) {
            assignedTags.remove(selection.get());
        } else {
            assignedTags.add(selection.get());
        }
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
        filteredTags.clear();
        filteredTags.addAll(allTags.stream()
                .filter(tag -> tag.getName().contains(filter))
                .map(SelectableTag::new)
                .collect(Collectors.toList()));
        selectedTagIndex = getSelectedTagIndexAfterFilter(filteredTags, filter);
        updateSelection(filteredTags, selectedTagIndex);
    }

    public ObservableList<Tag> getAssignedTags() {
        return assignedTags;
    }

    public ObservableList<SelectableTag> getFilteredTags() {
        return filteredTags;
    }

    private List<SelectableTag> convertToSelectableTags(List<Tag> allTags) {
        return allTags.stream()
                .map(SelectableTag::new)
                .collect(Collectors.toList());
    }

    private Optional<SelectableTag> getSelection() {
        return filteredTags.stream()
                .filter(SelectableTag::isSelected)
                .findFirst();
    }

    private void updateSelection(List<SelectableTag> tagList, Optional<Integer> tagIndex) {
        clearSelection(tagList);
        if (!tagIndex.isPresent()) return;
        int tagIndexToSelect = tagIndex.get();
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

    /**
     * Gets the list of tags that appears in newContactTags but not in originalTags
     */
    private List<SelectableTag> getNewMatchingTags(List<SelectableTag> originalTags, List<SelectableTag> updatedTags) {
        return updatedTags.stream()
                .filter(newContactTag -> !originalTags.contains(newContactTag))
                .collect(Collectors.toList());
    }
}
