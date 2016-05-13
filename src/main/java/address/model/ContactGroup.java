package address.model;

import javafx.beans.property.SimpleStringProperty;

public final class ContactGroup implements UniqueCopyable<ContactGroup> {

    private final SimpleStringProperty name;

    public ContactGroup() {
        this.name = new SimpleStringProperty("");
    }

    public ContactGroup(String name) {
        this.name = new SimpleStringProperty(name);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @Override
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
        if (this.getName() == other.getName()) return true;
        return this.getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return "Group : " + getName();
    }

    @Override
    public ContactGroup clone() {
        return new ContactGroup(getName());
    }
}
