package address.model;

import address.util.DateUtil;
import address.util.LocalDateAdapter;
import address.util.LocalDateTimeAdapter;

import javafx.beans.property.*;
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
public class Person extends DataType {

    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty street;
    private final IntegerProperty postalCode;
    private final StringProperty city;
    private final ObjectProperty<LocalDate> birthday;
    private final StringProperty githubUserName;
    private ObjectProperty<LocalDateTime> updatedAt;
    private final List<ContactGroup> contactGroups;

    /**
     * Default constructor.
     */
    public Person() {
        this("", "");
    }

    /**
     * Constructor with firstName and lastName parameters
     * Other parameters are set to "", or null if not a String
     * 
     * @param firstName
     * @param lastName
     */
    public Person(String firstName, String lastName) {
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);

        this.street = new SimpleStringProperty("");
        this.postalCode = new SimpleIntegerProperty();
        this.city = new SimpleStringProperty();
        this.birthday = new SimpleObjectProperty<>();
        this.contactGroups = new ArrayList<>();
        this.updatedAt = new SimpleObjectProperty<>(LocalDateTime.now());
        this.githubUserName = new SimpleStringProperty("");
    }

    /**
     * Deep copy constructor
     * @param person
     */
    public Person(Person person) {
        this.firstName = new SimpleStringProperty(person.getFirstName());
        this.lastName = new SimpleStringProperty(person.getLastName());

        this.street = new SimpleStringProperty(person.getStreet());
        this.postalCode = new SimpleIntegerProperty(person.getPostalCode());
        this.city = new SimpleStringProperty(person.getCity());
        this.birthday = new SimpleObjectProperty<>(person.getBirthday());
        this.contactGroups = new ArrayList<>(person.getContactGroupsCopy());
        this.updatedAt = new SimpleObjectProperty<>(person.getUpdatedAt());
        this.githubUserName = new SimpleStringProperty(person.getGithubUserName());
    }

    /**
     * @return a deep copy of the contactGroups
     */
    public List<ContactGroup> getContactGroupsCopy() {
        final List<ContactGroup> copy = new ArrayList<>();
        contactGroups.forEach((cg) -> copy.add(cg));
        return copy;
    }

    /**
     * Note: references point back to argument list (no defensive copying)
     * @param contactGroups
     */
    public void setContactGroups(List<ContactGroup> contactGroups) {
        this.contactGroups.clear();
        this.contactGroups.addAll(contactGroups);
        updatedAt.set(LocalDateTime.now());
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
        updatedAt.set(LocalDateTime.now());
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
        updatedAt.set(LocalDateTime.now());
    }

    public String getFullName() {
        return getFirstName() + ' ' + getLastName();
    }

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

    public Integer getPostalCode() {
        return postalCode.get();
    }

    public String getPostalCodeString() {
        return postalCode.getValue() == 0.0 ? "" : Integer.toString(postalCode.get());
    }

    public void setPostalCode(Integer postalCode) {
        this.postalCode.setValue(postalCode);
        updatedAt.set(LocalDateTime.now());
    }

    public IntegerProperty postalCodeProperty() {
        return postalCode;
    }

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

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    public LocalDate getBirthday() {
        return birthday.get();
    }

    public String getBirthdayString() {
        if (birthday.getValue() == null) return "";
        return DateUtil.format(birthday.getValue());
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday.set(birthday);
        updatedAt.set(LocalDateTime.now());
    }

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }

    public String getGithubUserName() {
        return githubUserName.get();
    }

    public StringProperty githubUserNameProperty() {
        return githubUserName;
    }

    /**
     * Precond: github username must be validated as a parsable url in the form of
     * https://www.github.com/ + githubUserName
     * @param githubUserName
     */
    public void setGithubUserName(String githubUserName) {
        this.githubUserName.set(githubUserName);
        updatedAt.set(LocalDateTime.now());
    }

    public ObjectProperty<LocalDate> birthdayProperty() {
        return birthday;
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
        setBirthday(updated.getBirthday());
        setContactGroups(updated.getContactGroupsCopy());
        setGithubUserName(updated.getGithubUserName());
        updatedAt.set(updated.getUpdatedAt());
        return this;
    }

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
        return "Person: " + getFullName();
    }

}
