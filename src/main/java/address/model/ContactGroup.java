package address.model;

import javafx.beans.property.SimpleStringProperty;

public class ContactGroup {
    SimpleStringProperty name;

    public ContactGroup() {
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
        if (otherGroup == null) {
            return false;
        }
        return this.getName().equals(((ContactGroup) otherGroup).getName());
    }
}
