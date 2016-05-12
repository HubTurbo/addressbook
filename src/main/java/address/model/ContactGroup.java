package address.model;

import javafx.beans.property.SimpleStringProperty;

public class ContactGroup {
    SimpleStringProperty name;

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
        this.name = new SimpleStringProperty(name);
    }

    public void update(ContactGroup group) {
        setName(group.getName());
    }

    @Override
    public boolean equals(Object otherGroup){
        if (!(otherGroup instanceof ContactGroup)) return false;
        if (otherGroup == this) return true;
        return this.getName().equals(((ContactGroup) otherGroup).getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode() * 39 + getName().hashCode() % 97;
    }
}
