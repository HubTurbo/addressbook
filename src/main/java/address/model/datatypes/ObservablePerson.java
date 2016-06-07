package address.model.datatypes;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import java.time.LocalDate;

/**
 * Allows access to the Person domain object's data as javafx properties and collections for easy binding and listening.
 */
public interface ObservablePerson extends ReadablePerson {

    StringProperty firstNameProperty();
    StringProperty lastNameProperty();
    StringProperty githubUserNameProperty();

    StringProperty streetProperty();
    StringProperty postalCodeProperty();
    StringProperty cityProperty();

    ObjectProperty<LocalDate> birthdayProperty();

    /**
     * @see ReadablePerson#getTags()
     * @return a javafx ObservableList view of this Person's tags
     */
    ObservableList<Tag> getTags();
}
