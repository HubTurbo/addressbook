package address.model.datatypes;

import address.util.DateTimeUtil;
import address.util.LocalDateAdapter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.teamdev.jxbrowser.chromium.internal.URLUtil;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

/**
 * Data-model implementation class representing the "Person" domain object.
 *
 * As far as possible, avoid working directly with this class.
 * Instead, use and declare the minimum required superclass/interface.
 *
 * Eg. A GUI element controller that only needs access to the Person's properties should declare the received Person
 * as an ObservablePerson -- since it does not need the functionality in the other superclasses/interfaces.
 */
public class Person extends BaseDataType implements ReadablePerson, WritablePerson, ObservablePerson {

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
     * Create with default values.
     * All primitive based properties are set to language defaults.
     * All string based properties are set to the empty string.
     * All object based properties are set to null.
     * All collections are set to an empty chosen collection implementation.
     */
    public Person() {}

    /**
     * Constructor with firstName and lastName parameters.
     * Other parameters are set to defaults.
     *
     * @see Person#Person()
     */
    public Person(String firstName, String lastName) {
        setFirstName(firstName);
        setLastName(lastName);
    }

    /**
     * Deep copy constructor.
     *
     * @see Person#update(ReadablePerson)
     */
    public Person(ReadablePerson toBeCopied) {
        update(toBeCopied);
    }

    /**
     * {@inheritDoc}
     *
     * @see WritablePerson#update(ReadablePerson)
     * @return self (calling this from a Person returns a Person instead of just a WritablePerson)
     */
    @Override
    public Person update(ReadablePerson newDataSource) {
        setFirstName(newDataSource.getFirstName());
        setLastName(newDataSource.getLastName());

        setStreet(newDataSource.getStreet());
        setPostalCode(newDataSource.getPostalCode());
        setCity(newDataSource.getCity());
        setGithubUserName(newDataSource.getGithubUserName());

        setBirthday(newDataSource.getBirthday());
        setTags(newDataSource.getTags());
        return this;
    }

//// NAME

    @JsonProperty("firstName")
    @Override
    public String getFirstName() {
        return firstName.get();
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    @Override
    public StringProperty firstNameProperty() {
        return firstName;
    }

    @JsonProperty("lastName")
    @Override
    public String getLastName() {
        return lastName.get();
    }

    @Override
    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    @Override
    public StringProperty lastNameProperty() {
        return lastName;
    }

    @Override
    public String fullName() {
        return getFirstName() + ' ' + getLastName();
    }

//// GITHUB USERNAME

    @JsonProperty("githubUsername")
    @Override
    public String getGithubUserName() {
        return githubUserName.get();
    }

    @Override
    public void setGithubUserName(String githubUserName) {
        this.githubUserName.set(githubUserName);
    }

    @Override
    public StringProperty githubUserNameProperty() {
        return githubUserName;
    }

    @Override
    public String githubProfilePageUrl(){
        return "https://www.github.com/" + githubUserName.get();
    }

    @Override
    public Optional<String> githubProfilePicUrl() {
        if (getGithubUserName().length() > 0) {
            String profilePicUrl = githubProfilePageUrl() + ".png";
            if (URLUtil.isURIFormat(profilePicUrl)){
                return Optional.of(profilePicUrl);
            }
        }
        return Optional.empty();
    }

    //// STREET

    @JsonProperty("street")
    @Override
    public String getStreet() {
        return street.get();
    }

    @Override
    public void setStreet(String street) {
        this.street.set(street);
    }

    @Override
    public StringProperty streetProperty() {
        return street;
    }

//// POSTAL CODE

    @JsonProperty("postalCode")
    @Override
    public String getPostalCode() {
        return postalCode.get();
    }

    @Override
    public void setPostalCode(String postalCode) {
        this.postalCode.set(postalCode);
    }

    @Override
    public StringProperty postalCodeProperty() {
        return postalCode;
    }

//// CITY

    @JsonProperty("city")
    @Override
    public String getCity() {
        return city.get();
    }

    @Override
    public void setCity(String city) {
        this.city.set(city);
    }

    @Override
    public StringProperty cityProperty() {
        return city;
    }

//// BIRTHDAY

    @JsonProperty("birthday")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @Override
    public LocalDate getBirthday() {
        return birthday.get();
    }

    @JsonSetter("birthday")
    @Override
    public void setBirthday(LocalDate birthday) {
        this.birthday.set(birthday);
    }

    @Override
    public ObjectProperty<LocalDate> birthdayProperty() {
        return birthday;
    }

    @Override
    public String birthdayString() {
        if (birthday.getValue() == null) return "";
        return DateTimeUtil.format(birthday.getValue());
    }

//// TAGS

    @JsonProperty("tags")
    @Override
    public ObservableList<Tag> getTags() {
        return tags;
    }

    @Override
    public void setTags(Collection<Tag> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }

    @Override
    public String tagsString() {
        final StringBuffer buffer = new StringBuffer();
        final String separator = ", ";
        tags.forEach(tag -> buffer.append(tag).append(separator));
        if (buffer.length() == 0) {
            return "";
        } else {
            return buffer.substring(0, buffer.length() - separator.length());
        }
    }

//// OTHER LOGIC

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (Person.class.isAssignableFrom(other.getClass())) {
            final ReadablePerson otherPerson = (ReadablePerson) other;
            return this.getFirstName().equals(otherPerson.getFirstName())
                    && this.getLastName().equals(otherPerson.getLastName());
        }
        if (ViewablePerson.class.isAssignableFrom(other.getClass())) {
            return other.equals(this);
        }
        return false;
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
