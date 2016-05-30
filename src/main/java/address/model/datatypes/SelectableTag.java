package address.model.datatypes;

// TODO CHANGE THIS TO WRAPPER CLASS
public class SelectableTag extends Tag {
    private boolean isSelected = false;

    public SelectableTag(Tag tag) {
        super(tag.getName());
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof SelectableTag)) return false;
        if (obj == this) return true;
        SelectableTag otherTag = (SelectableTag) obj;
        return this.getName().equals(otherTag.getName())
                && this.isSelected() == otherTag.isSelected();
    }

    @Override
    public int hashCode() {
        return getName().hashCode() * 39 + getName().hashCode() % 97 + (isSelected() ? 3 : 7);
    }
}
