package address.model.datatypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;

public class ContactGroup extends BaseDataType {

    @JsonIgnore private final SimpleStringProperty name;

    {
        name = new SimpleStringProperty("");
    }

    public ContactGroup() {}

    public ContactGroup(String name) {
        setName(name);
    }

    // Copy constructor
    public ContactGroup(ContactGroup grp) {
        update(grp);
    }

    public ContactGroup update(ContactGroup group) {
        setName(group.getName());
        return this;
    }

    @Override
    public List<Property> getPropertiesInOrder() {
        final List<Property> props = new ArrayList<>();
        props.add(name);
        return props;
    }

    @JsonProperty("name")
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public SimpleStringProperty nameProperty() {
        return name;
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

    @Override
    public ContactGroup clone() {
        return new ContactGroup(this);
    }
}
