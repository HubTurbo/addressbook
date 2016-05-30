package address.model.datatypes;

import address.util.LocalDateTimeAdapter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;

public class Tag extends UniqueData {

    @JsonIgnore private final SimpleStringProperty name;
    @JsonIgnore private final SimpleObjectProperty<LocalDateTime> updatedAt;

    public Tag(String name) {
        this.name = new SimpleStringProperty(name);
        this.updatedAt = new SimpleObjectProperty<>(LocalDateTime.now());
    }

    public Tag(String name, LocalDateTime updatedAt) {
        this.name = new SimpleStringProperty(name);
        this.updatedAt = new SimpleObjectProperty<>(updatedAt);
    }

    public Tag() {
        this("");
    }

    // Copy constructor
    public Tag(Tag tag) {
        name = new SimpleStringProperty(tag.getName());
        updatedAt = new SimpleObjectProperty<>(tag.getUpdatedAt());
    }

    @JsonProperty("name")
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
        setUpdatedAt(LocalDateTime.now());
    }

    @JsonProperty("updatedAt")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }

    @JsonSetter("updatedAt")
    private void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt.set(updatedAt);
    }

    public Tag update(Tag tag) {
        setName(tag.getName());
        setUpdatedAt(tag.getUpdatedAt());
        return this;
    }

    @Override
    public boolean equals(Object otherTag){
        if (otherTag == this) return true;
        if (otherTag == null) return false;
        if (!Tag.class.isAssignableFrom(otherTag.getClass())) return false;

        final Tag other = (Tag) otherTag;
        return this.getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return "Tag: " + getName();
    }

}
