package address.model.datatypes.person;

import address.model.datatypes.ExtractableObservables;
import address.model.datatypes.tag.Tag;
import address.util.collections.UnmodifiableObservableList;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableList;

import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Allows read-only access to the Person domain object's data.
 */
public interface ReadOnlyPerson extends ExtractableObservables {

    String getFirstName();
    String getLastName();
    /**
     * @return first-last format full name
     */
    String fullName();

    String getGithubUserName();
    /**
     * @return github profile url
     */
    URL profilePageUrl();
    Optional<String> githubProfilePicUrl();

    String getStreet();
    String getPostalCode();
    String getCity();

    LocalDate getBirthday();
    /**
     * @return birthday date-formatted as string
     */
    String birthdayString();

    /**
     * @return string representation of this Person's tags
     */
    String tagsString();

    ReadOnlyStringProperty firstNameProperty();
    ReadOnlyStringProperty lastNameProperty();
    ReadOnlyStringProperty githubUserNameProperty();

    ReadOnlyStringProperty streetProperty();
    ReadOnlyStringProperty postalCodeProperty();
    ReadOnlyStringProperty cityProperty();

    ReadOnlyObjectProperty<LocalDate> birthdayProperty();

    /**
     * @return ObservableList unmodifiable view of this Person's tags
     * TODO ReadOnlyTag class
     */
    UnmodifiableObservableList<Tag> getTags();

    @Override
    default Observable[] extractObservables() {
        return new Observable[] {
                firstNameProperty(),
                lastNameProperty(),
                githubUserNameProperty(),

                streetProperty(),
                postalCodeProperty(),
                cityProperty(),

                birthdayProperty(),
                getTags()
        };
    }
}
