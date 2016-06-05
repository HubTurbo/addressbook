package address.model.datatypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;

public class Tag extends BaseDataType {

    @JsonIgnore private final SimpleStringProperty name;

    {
        name = new SimpleStringProperty("");
    }

    public Tag() {}

    public Tag(String name) {
        setName(name);
    }

    // Copy constructor
    public Tag(Tag grp) {
        update(grp);
    }

    public Tag update(Tag group) {
        setName(group.getName());
        return this;
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
        if (!Tag.class.isAssignableFrom(otherGroup.getClass())) return false;

        final Tag other = (Tag) otherGroup;
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
    public Tag clone() {
        return new Tag(this);
    }
}
