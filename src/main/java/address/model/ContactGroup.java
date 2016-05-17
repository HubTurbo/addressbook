package address.model;

import address.util.LocalDateTimeAdapter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;

public class ContactGroup extends DataType {

    private final SimpleStringProperty name;
    private final SimpleObjectProperty<LocalDateTime> updatedAt;

    public ContactGroup(String name) {
        this.name = new SimpleStringProperty(name);
        this.updatedAt = new SimpleObjectProperty<>(LocalDateTime.now());
    }

    public ContactGroup(String name, LocalDateTime updatedAt) {
        this.name = new SimpleStringProperty(name);
        this.updatedAt = new SimpleObjectProperty<>(updatedAt);
    }

    public ContactGroup() {
        this("");
    }

    // Copy constructor
    public ContactGroup(ContactGroup grp) {
        name = new SimpleStringProperty(grp.getName());
        updatedAt = new SimpleObjectProperty<>(grp.getUpdatedAt());
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
        updatedAt.set(LocalDateTime.now());
    }

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }

    public ContactGroup update(ContactGroup group) {
        setName(group.getName());
        updatedAt.set(group.getUpdatedAt());
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
        return "Group: " + getName();
    }

}
