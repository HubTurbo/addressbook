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
}