package address.events;

import address.model.datatypes.tag.SelectableTag;

import java.util.List;

public class TagListChangedEvent extends BaseEvent {
    List<SelectableTag> resultTag;

    public TagListChangedEvent(List<SelectableTag> resultTag) {
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
