package address.model;

public class SelectableContactGroup extends ContactGroup {
    private boolean isSelected = false;

    public SelectableContactGroup(ContactGroup contactGroup) {
        super(contactGroup.getName());
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
        if (!(obj instanceof SelectableContactGroup)) return false;
        if (obj == this) return true;
        SelectableContactGroup otherGroup = (SelectableContactGroup) obj;
        return this.getName().equals(otherGroup.getName())
                && this.isSelected() == otherGroup.isSelected();
    }

    @Override
    public int hashCode() {
        return getName().hashCode() * 39 + getName().hashCode() % 97 + (isSelected() ? 3 : 7);
    }
}
