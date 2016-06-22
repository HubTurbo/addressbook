package address.events;

import address.model.datatypes.tag.SelectableTag;

import java.util.List;

public class TagSelectionListChangedEvent extends BaseEvent {
    List<SelectableTag> resultTag;

    public TagSelectionListChangedEvent(List<SelectableTag> resultTag) {
        this.resultTag = resultTag;
    }

    public List<SelectableTag> getResultTag() {
        return this.resultTag;
    }

    @Override
    public String toString() {
        return "Tags selection has changed, now " + resultTag.size() + " items.";
    }
}
