package address.model;

import javafx.beans.property.SimpleStringProperty;

public class ContactGroup extends UniqueData {

    private final SimpleStringProperty name;

    {
        name = new SimpleStringProperty("");
    }

    public ContactGroup() {}

    public ContactGroup(String name) {
        this();
        this.name.set(name);
    }

    // Copy constructor
    public ContactGroup(ContactGroup grp) {
        this();
        update(grp);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public ContactGroup update(ContactGroup group) {
        setName(group.getName());
        return this;
    }

    @Override
    public boolean equals(Object otherGroup){
        if (otherGroup == this) return true;
        if (otherGroup == null) return false;
        if (!ContactGroup.class.isAssignableFrom(otherGroup.getClass())) return false;

        final ContactGroup other = (ContactGroup) otherGroup;
        return this.getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return "Group: " + getName();
    }

}
