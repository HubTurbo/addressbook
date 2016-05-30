package address.events;

import address.model.datatypes.SelectableTag;

import java.util.List;

public class TagsChangedEvent extends BaseEvent {
    List<SelectableTag> resultTag;

    public TagsChangedEvent(List<SelectableTag> resultTag) {
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
