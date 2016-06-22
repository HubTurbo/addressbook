package address.events;

import address.model.datatypes.tag.SelectableTag;

import java.util.List;

public class TagSelectionSearchResultsChangedEvent extends BaseEvent {
    List<SelectableTag> selectableContactTags;

    public TagSelectionSearchResultsChangedEvent(List<SelectableTag> selectableContactTags) {
        this.selectableContactTags = selectableContactTags;
    }

    public List<SelectableTag> getSelectableTags() {
        return this.selectableContactTags;
    }

    @Override
    public String toString() {
        return "Tag search results have changed: now " + selectableContactTags.size() + " items.";
    }
}
