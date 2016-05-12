package address.model;

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
public class Person {

    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty street;
    private final IntegerProperty postalCode;
    private final StringProperty city;
    private final ObjectProperty<LocalDate> birthday;
    private ObjectProperty<LocalDateTime> updatedAt;
    private final List<ContactGroup> contactGroups;

    /**
     * Default constructor.
     */
    public Person() {
        this(null, null);
    }

    /**
     * Constructor with some initial data.
     * 
     * @param firstName
     * @param lastName
     */
    public Person(String firstName, String lastName) {
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);

        // Some initial dummy data, just for convenient testing.
        this.street = new SimpleStringProperty("some street");
        this.postalCode = new SimpleIntegerProperty(1234);
        this.city = new SimpleStringProperty("some city");
        this.birthday = new SimpleObjectProperty<>(LocalDate.of(1999, 2, 21));
        this.contactGroups = new ArrayList<>();
        contactGroups.add(new ContactGroup("friends"));
        this.updatedAt = new SimpleObjectProperty<>(LocalDateTime.now());
    }

    /**
     * Copy constructor
     * @param person
     */
    public Person(Person person) {
        this.firstName = new SimpleStringProperty(person.getFirstName());
        this.lastName = new SimpleStringProperty(person.getLastName());

        this.street = new SimpleStringProperty(person.getStreet());
        this.postalCode = new SimpleIntegerProperty(person.getPostalCode());
        this.city = new SimpleStringProperty(person.getCity());
        this.birthday = new SimpleObjectProperty<>(person.getBirthday());
        this.contactGroups = new ArrayList<>(person.getContactGroups());
        this.updatedAt = new SimpleObjectProperty<>(person.getUpdatedAt());
    }

    public List<ContactGroup> getContactGroups() {
        return contactGroups;
    }

    public void setContactGroups(List<ContactGroup> contactGroups) {
        this.contactGroups.clear();
        this.contactGroups.addAll(contactGroups);
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public String getStreet() {
        return street.get();
    }

    public void setStreet(String street) {
        this.street.set(street);
    }

    public StringProperty streetProperty() {
        return street;
    }

    public int getPostalCode() {
        return postalCode.get();
    }

    public void setPostalCode(int postalCode) {
        this.postalCode.set(postalCode);
    }

    public IntegerProperty postalCodeProperty() {
        return postalCode;
    }

    public String getCity() {
        return city.get();
    }

    public void setCity(String city) {
        this.city.set(city);
    }

    public StringProperty cityProperty() {
        return city;
    }

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    public LocalDate getBirthday() {
        return birthday.get();
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday.set(birthday);
    }

    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }

    public void setUpdatedAt(LocalDateTime birthday) {
        this.updatedAt.set(birthday);
    }

    public ObjectProperty<LocalDate> birthdayProperty() {
        return birthday;
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
        int hash = 3;
        hash = 53 * hash + (this.getFirstName() != null ? this.getFirstName().hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("Person : %1$s %2$s", getFirstName(), getLastName());
    }

    /**
     * Updates the attributes based on the values in the parameter.
     * @param updated The object containing the new attributes.
     */
    public void update(Person updated) {
        setFirstName(updated.getFirstName());
        setLastName(updated.getLastName());
        setStreet(updated.getStreet());
        setPostalCode(updated.getPostalCode());
        setCity(updated.getCity());
        setBirthday(updated.getBirthday());
        setUpdatedAt(updated.getUpdatedAt());
        setContactGroups(updated.getContactGroups());
    }
}
