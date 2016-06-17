package address.model.datatypes.person;

import address.model.datatypes.ExtractableObservables;
import address.model.datatypes.tag.Tag;
import address.util.collections.UnmodifiableObservableList;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
     * @return unmodifiable list view of tags.
     */
    List<Tag> getTagList();
    /**
     * @return string representation of this Person's tags
     */
    String tagsString();

//// Operations below are optional; override if they will be needed.
    
    default ReadOnlyStringProperty firstNameProperty() {
        throw new UnsupportedOperationException();
    }
    default ReadOnlyStringProperty lastNameProperty() {
        throw new UnsupportedOperationException();
    }
    default ReadOnlyStringProperty githubUserNameProperty() {
        throw new UnsupportedOperationException();
    }

    default ReadOnlyStringProperty streetProperty() {
        throw new UnsupportedOperationException();
    }
    default ReadOnlyStringProperty postalCodeProperty() {
        throw new UnsupportedOperationException();
    }
    default ReadOnlyStringProperty cityProperty() {
        throw new UnsupportedOperationException();
    }

    default ReadOnlyObjectProperty<LocalDate> birthdayProperty() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return ObservableList unmodifiable view of this Person's tags
     */
    default UnmodifiableObservableList<Tag> getObservableTagList() {
        throw new UnsupportedOperationException();
    }

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
                getObservableTagList()
        };
    }
}
