package address.model;

import address.util.DateTimeUtil;
import address.util.LocalDateAdapter;
import address.util.LocalDateTimeAdapter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-type class representing a person
 */
public class Person extends BaseDataType {

    @JsonIgnore private final StringProperty firstName;
    @JsonIgnore private final StringProperty lastName;

    @JsonIgnore private final StringProperty street;
    @JsonIgnore private final StringProperty postalCode;
    @JsonIgnore private final StringProperty city;
    @JsonIgnore private final StringProperty githubUserName;

    @JsonIgnore private final ObjectProperty<LocalDate> birthday;
    @JsonIgnore private final ObjectProperty<LocalDateTime> updatedAt;
    @JsonIgnore private final ObservableList<ContactGroup> contactGroups;

    // defaults
    {
        firstName = new SimpleStringProperty("");
        lastName = new SimpleStringProperty("");

        street = new SimpleStringProperty("");
        postalCode = new SimpleStringProperty("");
        city = new SimpleStringProperty("");
        githubUserName = new SimpleStringProperty("");

        birthday = new SimpleObjectProperty<>();
        updatedAt = new SimpleObjectProperty<>(LocalDateTime.now());
        contactGroups = FXCollections.observableArrayList();
    }    
    
    /**
     * Default constructor.
     */
    public Person() {}

    /**
     * Constructor with firstName and lastName parameters
     * Other parameters are set to "", or null if not a String
     * 
     * @param firstName
     * @param lastName
     */
    public Person(String firstName, String lastName) {
        setFirstName(firstName);
        setLastName(lastName);
    }

    /**
     * Deep copy constructor
     * @param person
     */
    public Person(Person person) {
        update(person);
    }

    /**
     * Updates the attributes based on the values in the parameter.
     * Mutable references are cloned.
     *
     * @param updated The object containing the new attributes.
     * @return self
     */
    public Person update(Person updated) {
        setFirstName(updated.getFirstName());
        setLastName(updated.getLastName());

        setStreet(updated.getStreet());
        setPostalCode(updated.getPostalCode());
        setCity(updated.getCity());
        setGithubUserName(updated.getGithubUserName());

        setBirthday(updated.getBirthday());
        setUpdatedAt(updated.getUpdatedAt());
        setContactGroups(updated.getContactGroups());
        return this;
    }

    @Override
    public List<Property> getProperties() {
        final List<Property> props = new ArrayList<>();
        props.add(firstName);
        props.add(lastName);

        return props;
    }

//// NAME
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
        updatedAt.set(LocalDateTime.now());
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    @JsonProperty("lastName")
    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
        updatedAt.set(LocalDateTime.now());
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public String fullName() {
        return getFirstName() + ' ' + getLastName();
    }

//// STREET
    @JsonProperty("street")
    public String getStreet() {
        return street.get();
    }

    public void setStreet(String street) {
        this.street.set(street);
        updatedAt.set(LocalDateTime.now());
    }

    public StringProperty streetProperty() {
        return street;
    }

//// POSTAL CODE
    @JsonProperty("postalCode")
    public String getPostalCode() {
        return postalCode.get();
    }

    public void setPostalCode(String postalCode) {
        this.postalCode.set(postalCode);
        updatedAt.set(LocalDateTime.now());
    }

    public StringProperty postalCodeProperty() {
        return postalCode;
    }

//// CITY
    @JsonProperty("city")
    public String getCity() {
        return city.get();
    }

    public void setCity(String city) {
        this.city.set(city);
        updatedAt.set(LocalDateTime.now());
    }

    public StringProperty cityProperty() {
        return city;
    }

//// BIRTHDAY
    @JsonProperty("birthday")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    public LocalDate getBirthday() {
        return birthday.get();
    }

    @JsonSetter("birthday")
    public void setBirthday(LocalDate birthday) {
        this.birthday.set(birthday);
        updatedAt.set(LocalDateTime.now());
    }

    public ObjectProperty<LocalDate> birthdayProperty() {
        return birthday;
    }

    public String birthdayString() {
        if (birthday.getValue() == null) return "";
        return DateTimeUtil.format(birthday.getValue());
    }

//// GITHUB USERNAME
    @JsonProperty("githubUsername")
    public String getGithubUserName() {
        return githubUserName.get();
    }

    /**
     * Precond: GitHub username must be validated as a parsable url in the form of
     * https://www.github.com/ + githubUserName
     * TODO make a custom githubusername class that validates the string on creation so no checks needed
     * @param githubUserName
     */
    public void setGithubUserName(String githubUserName) {
        this.githubUserName.set(githubUserName);
        updatedAt.set(LocalDateTime.now());
    }

    public StringProperty githubUserNameProperty() {
        return githubUserName;
    }

    public String profilePageUrl(){
        return "https://www.github.com/" + githubUserName.get();
    }

//// CONTACT GROUPS
    @JsonProperty("contactGroups")
    public ObservableList<ContactGroup> getContactGroups() {
        return contactGroups;
    }

    /**
     * Note: references point back to argument list (no defensive copying)
     * internal list is updated with elements in the argument list instead of being wholly replaced
     * @param contactGroups
     */
    public void setContactGroups(List<ContactGroup> contactGroups) {
        this.contactGroups.clear();
        this.contactGroups.addAll(contactGroups);
        updatedAt.set(LocalDateTime.now());
    }

    public String contactGroupsString() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < contactGroups.size(); i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            buffer.append(contactGroups.get(i).getName());
        }
        return buffer.toString();
    }

//// UPDATED AT
    @JsonProperty("updatedAt")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }

    public ObjectProperty<LocalDateTime> updatedAtProperty() {
        return updatedAt;
    }

    @JsonSetter("updatedAt")
    public void setUpdatedAt(LocalDateTime lastUpdated) {
        updatedAt.set(lastUpdated);
    }

//// OTHER LOGIC

    @Override
    public boolean equals(Object otherPerson){
        if (otherPerson == this) return true;
        if (otherPerson == null) return false;
        if (!Person.class.isAssignableFrom(otherPerson.getClass())) return false;

        final Person other = (Person) otherPerson;
        return this.getFirstName().equals(other.getFirstName()) && this.getLastName().equals(other.getLastName());
    }

    @Override
    public int hashCode() {
        return (getFirstName() + getLastName()).hashCode();
    }

    @Override
    public String toString() {
        return "Person: " + fullName();
    }

    @Override
    public Person clone() {
        return new Person(this);
    }
}
