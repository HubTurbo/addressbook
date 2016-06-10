package address.model.datatypes.person;

import address.model.datatypes.BaseDataType;
import address.model.datatypes.tag.Tag;
import address.util.DateTimeUtil;
import address.util.LocalDateAdapter;

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
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Data-model implementation class representing the "Person" domain object.
 *
 * As far as possible, avoid working directly with this class.
 * Instead, use and declare the minimum required superclass/interface.
 *
 * Eg. A GUI element controller that only needs access to the Person's properties should declare the received Person
 * as an ReadOnlyPerson -- since it does not need the functionality in the other superclasses/interfaces.
 */
public class Person extends BaseDataType implements ReadOnlyPerson {

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
     * @see Person#update(ReadOnlyPerson)
     */
    public Person(ReadOnlyPerson toBeCopied) {
        update(toBeCopied);
    }

    /**
     * {@inheritDoc}
     *
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
        setTags(newDataSource.getTags());
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

    public void setGithubUserName(String githubUserName) {
        this.githubUserName.set(githubUserName);
    }

    @Override
    public ReadOnlyStringProperty githubUserNameProperty() {
        return githubUserName;
    }

    @Override
    public URL profilePageUrl(){
        URL url = null;

        try {
            url = new URL("https://github.com/" + githubUserName.get());
        } catch (MalformedURLException e) {
            try {
                url = new URL("https://github.com");
            } catch (MalformedURLException e1) {
                assert false;
            }
        }
        return url;
    }

    @Override
    public Optional<String> githubProfilePicUrl() {
        if (getGithubUserName().length() > 0) {
            String profilePicUrl = profilePageUrl().toExternalForm() + ".png";
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
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
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

    @Override
    public String birthdayString() {
        if (birthday.getValue() == null) return "";
        return DateTimeUtil.format(birthday.getValue());
    }

//// TAGS

    @JsonProperty("tags")
    @Override
    public UnmodifiableObservableList<Tag> getTags() {
        return new UnmodifiableObservableList<>(tags);
    }

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
            final ReadOnlyPerson otherPerson = (ReadOnlyPerson) other;
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
