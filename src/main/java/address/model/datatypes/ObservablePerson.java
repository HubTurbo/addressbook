package address.model.datatypes;

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

    // TODO: consider using reflection to access all isassignablefrom(Property) returning methods for maintainability
    /**
     * Passes matching property field pairs (paired between self and another ObservablePerson) as arguments to a callback.
     * The callback is called once for each property field in the ObservabePerson class.
     *
     * @see #forEachObservableListFieldPairWith(ObservablePerson, BiConsumer)
     * @param other the ObservablePerson whose property fields make up the second parts of the property pairs
     * @param action called for every property field: action(self:property, other:same_property)
     *               first argument is from self, second is from the "other" parameter
     */
    default void forEachPropertyFieldPairWith(ObservablePerson other,
                                              BiConsumer<? super Property, ? super Property> action) {
        action.accept(firstNameProperty(), other.firstNameProperty());
        action.accept(lastNameProperty(), other.lastNameProperty());
        action.accept(githubUserNameProperty(), other.githubUserNameProperty());

        action.accept(streetProperty(), other.streetProperty());
        action.accept(postalCodeProperty(), other.postalCodeProperty());
        action.accept(cityProperty(), other.cityProperty());

        action.accept(birthdayProperty(), other.birthdayProperty());
    }

    /**
     * Same as {@link #forEachPropertyFieldPairWith(ObservablePerson, BiConsumer) forEachPropertyFieldPairWith}
     * but for ObservableList fields instead if Property fields.
     *
     * @see #forEachPropertyFieldPairWith(ObservablePerson, BiConsumer)
     * @param other see param "other" at {@link #forEachPropertyFieldPairWith(ObservablePerson, BiConsumer)
     *              forEachPropertyFieldPairWith}
     * @param action see param "action" at {@link #forEachPropertyFieldPairWith(ObservablePerson, BiConsumer)
     *               forEachPropertyFieldPairWith}
     */
    default void forEachObservableListFieldPairWith(ObservablePerson other,
                                                    BiConsumer<? super ObservableList, ? super ObservableList> action) {
        action.accept(getTags(), other.getTags());
    }
}
