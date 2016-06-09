package address.model.datatypes.person;

import address.model.datatypes.ExtractableObservables;
import address.model.datatypes.tag.Tag;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.function.BiConsumer;

/**
 * Allows access to the Person domain object's data as javafx properties and collections for easy binding and listening.
 * Also includes useful methods for working with fields from two ObservabblePersons together.
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
    String githubProfilePageUrl();
    String githubProfilePicUrl();

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

    StringProperty firstNameProperty();
    StringProperty lastNameProperty();
    StringProperty githubUserNameProperty();

    StringProperty streetProperty();
    StringProperty postalCodeProperty();
    StringProperty cityProperty();

    ObjectProperty<LocalDate> birthdayProperty();

    /**
     * @return ObservableList unmodifiable view of this Person's tags
     */
    ObservableList<Tag> getTags();

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

    // TODO: consider using reflection to access all isassignablefrom(Property) returning methods for maintainability
    /**
     * Passes matching property field pairs (paired between self and another ReadOnlyPerson) as arguments to a callback.
     * The callback is called once for each property field in the ObservabePerson class.
     *
     * @param other the ReadOnlyPerson whose property fields make up the second parts of the property pairs
     * @param action called for every property field: action(self:property, other:same_property)
     *               first argument is from self, second is from the "other" parameter
     */
    default void forEachPropertyFieldPairWith(ReadOnlyPerson other,
                                              BiConsumer<? super Property, ? super Property> action) {
        action.accept(firstNameProperty(), other.firstNameProperty());
        action.accept(lastNameProperty(), other.lastNameProperty());
        action.accept(githubUserNameProperty(), other.githubUserNameProperty());

        action.accept(streetProperty(), other.streetProperty());
        action.accept(postalCodeProperty(), other.postalCodeProperty());
        action.accept(cityProperty(), other.cityProperty());

        action.accept(birthdayProperty(), other.birthdayProperty());
    }

}
