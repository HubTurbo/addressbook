package address.model.datatypes.person;

import address.model.datatypes.UniqueData;
import address.model.datatypes.tag.Tag;
import address.util.DateTimeUtil;

import address.util.XmlLocalDateAdapter;
import address.util.collections.UnmodifiableObservableList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.teamdev.jxbrowser.chromium.internal.URLUtil;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Data-model implementation class representing the "Person" domain object.
 * ID is immutable, so this class can be safely used as map keys and set elements.
 *
 * As far as possible, avoid working directly with this class.
 * Instead, use and declare the minimum required superclass/interface.
 *
 * Eg. A GUI element controller that only needs access to the Person's properties should declare the received Person
 * as an ReadOnlyPerson -- since it does not need the functionality in the other superclasses/interfaces.
 */
public class Person extends UniqueData implements ReadOnlyPerson {

    private final int ID;

    @JsonIgnore private final SimpleStringProperty firstName;
    @JsonIgnore private final SimpleStringProperty lastName;

    @JsonIgnore private final SimpleStringProperty street;
    @JsonIgnore private final SimpleStringProperty postalCode;
    @JsonIgnore private final SimpleStringProperty city;
    @JsonIgnore private final SimpleStringProperty githubUserName;

    @JsonIgnore private final SimpleObjectProperty<LocalDate> birthday;
    @JsonIgnore private final ObservableList<Tag> tags;

    // defaults
    {
        firstName = new SimpleStringProperty("");
        lastName = new SimpleStringProperty("");

        street = new SimpleStringProperty("");
        postalCode = new SimpleStringProperty("");
        city = new SimpleStringProperty("");
        githubUserName = new SimpleStringProperty("");

        birthday = new SimpleObjectProperty<>();

        tags = FXCollections.observableArrayList();
    }

    /**
     * ID-less person data container
     */
    public static Person createPersonDataContainer() {
        return new Person(0);
    }

    public Person(int id) {
        this.ID = id;
    }

    /**
     * Constructor with firstName and lastName parameters.
     * Other parameters are set to defaults.
     */
    public Person(String firstName, String lastName, int id) {
        this(id);
        setFirstName(firstName);
        setLastName(lastName);
    }

    /**
     * Deep copy constructor. <strong>Also copies id.</strong>
     * @see Person#update(ReadOnlyPerson)
     */
    public Person(ReadOnlyPerson toBeCopied) {
        this(toBeCopied.getID());
        update(toBeCopied);
    }

    /**
     * Does not update own ID with argument's ID.
     * @return self (calling this from a Person returns a Person instead of just a WritablePerson)
     */
    public Person update(ReadOnlyPerson newDataSource) {
        setFirstName(newDataSource.getFirstName());
        setLastName(newDataSource.getLastName());

        setStreet(newDataSource.getStreet());
        setPostalCode(newDataSource.getPostalCode());
        setCity(newDataSource.getCity());
        setGithubUserName(newDataSource.getGithubUserName());

        setBirthday(newDataSource.getBirthday());
        setTags(newDataSource.getTagList());
        return this;
    }

    // TODO: consider using reflection to access all isassignablefrom(Property) returning methods for maintainability
    /**
     * Passes matching property field pairs (paired between self and another ReadOnlyPerson) as arguments to a callback.
     * The callback is called once for each property field in the ObservabePerson class.
     *
     * @param other the ReadOnlyPerson whose property fields make up the second parts of the property pairs
     * @param action called for every property field: action(self:property, other:same_property)
     *               first argument is from self, second is from the "other" parameter
     */
    public void forEachPropertyFieldPairWith(Person other, BiConsumer<? super Property, ? super Property> action) {
        action.accept(firstName, other.firstName);
        action.accept(lastName, other.lastName);
        action.accept(githubUserName, other.githubUserName);

        action.accept(street, other.street);
        action.accept(postalCode, other.postalCode);
        action.accept(city, other.city);

        action.accept(birthday, other.birthday);
    }

//// ID

    public int getID() {
        return ID;
    }

//// NAME

    @JsonProperty("firstName")
    @Override
    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    @Override
    public ReadOnlyStringProperty firstNameProperty() {
        return firstName;
    }

    @JsonProperty("lastName")
    @Override
    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    @Override
    public ReadOnlyStringProperty lastNameProperty() {
        return lastName;
    }

//// GITHUB USERNAME

    @JsonProperty("githubUsername")
    @Override
    public String getGithubUserName() {
        return githubUserName.get();
    }

    public void setGithubUserName(String githubUserName) {
        this.githubUserName.set(githubUserName);
    }

    @Override
    public ReadOnlyStringProperty githubUserNameProperty() {
        return githubUserName;
    }

//// STREET

    @JsonProperty("street")
    @Override
    public String getStreet() {
        return street.get();
    }

    public void setStreet(String street) {
        this.street.set(street);
    }

    @Override
    public ReadOnlyStringProperty streetProperty() {
        return street;
    }

//// POSTAL CODE

    @JsonProperty("postalCode")
    @Override
    public String getPostalCode() {
        return postalCode.get();
    }

    public void setPostalCode(String postalCode) {
        this.postalCode.set(postalCode);
    }

    @Override
    public ReadOnlyStringProperty postalCodeProperty() {
        return postalCode;
    }

//// CITY

    @JsonProperty("city")
    @Override
    public String getCity() {
        return city.get();
    }

    public void setCity(String city) {
        this.city.set(city);
    }

    @Override
    public ReadOnlyStringProperty cityProperty() {
        return city;
    }

//// BIRTHDAY

    @JsonProperty("birthday")
    @XmlJavaTypeAdapter(XmlLocalDateAdapter.class)
    @Override
    public LocalDate getBirthday() {
        return birthday.get();
    }

    @JsonSetter("birthday")
    public void setBirthday(LocalDate birthday) {
        this.birthday.set(birthday);
    }

    @Override
    public ReadOnlyObjectProperty<LocalDate> birthdayProperty() {
        return birthday;
    }

//// TAGS

    @Override
    public List<Tag> getTagList() {
        return Collections.unmodifiableList(tags);
    }

    @Override
    public UnmodifiableObservableList<Tag> getObservableTagList() {
        return new UnmodifiableObservableList<>(tags);
    }

    @JsonProperty("tags")
    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(Collection<Tag> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }

//// OTHER LOGIC

    /**
     * Compares id
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (Person.class.isAssignableFrom(other.getClass())) {
            final ReadOnlyPerson otherP = (ReadOnlyPerson) other;
            return getID() == otherP.getID();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ID;
    }

    @Override
    public String toString() {
        return "Person #" + ID + ": " + fullName();
    }

}
