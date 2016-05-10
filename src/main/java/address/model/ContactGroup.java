package address.model;

import javafx.beans.property.SimpleStringProperty;

public class ContactGroup {
    SimpleStringProperty name;

    public ContactGroup(String name) {
        this.name = new SimpleStringProperty(name);
    }
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name = new SimpleStringProperty(name);
    }
}
