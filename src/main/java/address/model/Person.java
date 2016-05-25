package address.model;

import address.util.DateUtil;
import address.util.LocalDateAdapter;
import address.util.LocalDateTimeAdapter;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class for a Person.
 *
 * @author Marco Jakob
 */
public class Person extends UniqueData {

    private final StringProperty firstName;
    private final StringProperty lastName;

    private final StringProperty street;
    private final IntegerProperty postalCode;
    private final StringProperty city;
    private final StringProperty githubUserName;

    private final ObjectProperty<LocalDate> birthday;
    private final ObjectProperty<LocalDateTime> updatedAt;
    private final ObservableList<ContactGroup> contactGroups;

    // defaults
    {
        firstName = new SimpleStringProperty("");
        lastName = new SimpleStringProperty("");

        street = new SimpleStringProperty("");
        postalCode = new SimpleIntegerProperty();
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

//// NAME

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

    public int getPostalCode() {
        return postalCode.get();
    }

    public void setPostalCode(int postalCode) {
        this.postalCode.set(postalCode);
        updatedAt.set(LocalDateTime.now());
    }

    public IntegerProperty postalCodeProperty() {
        return postalCode;
    }

    public String postalCodeString() {
        return postalCode.getValue() == 0 ? "" : Integer.toString(postalCode.get());
    }

//// CITY

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

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    public LocalDate getBirthday() {
        return birthday.get();
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday.set(birthday);
        updatedAt.set(LocalDateTime.now());
    }

    public ObjectProperty<LocalDate> birthdayProperty() {
        return birthday;
    }

    public String birthdayString() {
        if (birthday.getValue() == null) return "";
        return DateUtil.format(birthday.getValue());
    }

//// GITHUB USERNAME

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

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }

    public ObjectProperty<LocalDateTime> updatedAtProperty() {
        return updatedAt;
    }

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
        if (this.getFirstName() == other.getFirstName() && this.getLastName() == other.getLastName()) return true;
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

}
