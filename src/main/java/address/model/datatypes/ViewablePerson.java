package address.model.datatypes;

import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.time.LocalDate;

/**
 * Extends {@link Viewable} for the Person domain object.
 * @see Viewable
 */
public class ViewablePerson extends Viewable<Person> implements ObservableViewablePerson {

    public ViewablePerson(Person backingPerson) {
        super(backingPerson, Person::new);
    }

    @Override
    protected void conditionallyBindVisibleToBacking() {
        forceSyncFromBacking();
        backing.forEachPropertyFieldPairWith(visible, this::conditionallyBindValue);

        // all changes here result in removal of affected list elements and (re)adding of any updated/new elements
        // at their correct positions.
        backing.getTags().addListener((ListChangeListener<? super Tag>) change -> {
            while (change.next()) {
                int from = change.getFrom();
                int to = change.getTo();
                if (change.wasPermutated()) { // element reordering
                    visible.getTags().subList(from, to).clear();
                    visible.getTags().addAll(from, backing.getTags().subList(from, to));
                } else { // list/element mutation
                    visible.getTags().subList(from, from + change.getRemovedSize()).clear();
                    visible.getTags().addAll(from, backing.getTags().subList(from, from + change.getAddedSize()));
                }
            }
        });
    }

    @Override
    public void forceSyncFromBacking() {
        visible.update(backing);
    }

// APPLICATION STATE ACCESSORS
    // none yet

// PERSON ACCESSORS

    @Override
    public StringProperty firstNameProperty() {
        return visible.firstNameProperty();
    }

    @Override
    public StringProperty lastNameProperty() {
        return visible.lastNameProperty();
    }

    @Override
    public StringProperty githubUserNameProperty() {
        return visible.githubUserNameProperty();
    }

    @Override
    public StringProperty streetProperty() {
        return visible.streetProperty();
    }

    @Override
    public StringProperty postalCodeProperty() {
        return visible.postalCodeProperty();
    }

    @Override
    public StringProperty cityProperty() {
        return visible.cityProperty();
    }

    @Override
    public ObjectProperty<LocalDate> birthdayProperty() {
        return visible.birthdayProperty();
    }

    @Override
    public String getFirstName() {
        return visible.getFirstName();
    }

    @Override
    public String getLastName() {
        return visible.getLastName();
    }

    @Override
    public String fullName() {
        return visible.fullName();
    }

    @Override
    public String getGithubUserName() {
        return visible.getGithubUserName();
    }

    @Override
    public String profilePageUrl() {
        return visible.profilePageUrl();
    }

    @Override
    public String getStreet() {
        return visible.getStreet();
    }

    @Override
    public String getPostalCode() {
        return visible.getPostalCode();
    }

    @Override
    public String getCity() {
        return visible.getCity();
    }

    @Override
    public LocalDate getBirthday() {
        return visible.getBirthday();
    }

    @Override
    public String birthdayString() {
        return visible.birthdayString();
    }

    @Override
    public ObservableList<Tag> getTags() {
        return visible.getTags();
    }

    @Override
    public String tagsString() {
        return visible.tagsString();
    }


    /**
     * Use backing Person for comparison.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (ViewablePerson.class.isAssignableFrom(other.getClass())) {
            return backing.equals(((ViewablePerson) other).backing);
        }
        if (Person.class.isAssignableFrom(other.getClass())) {
            return backing.equals(other);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return backing.hashCode();
    }

    @Override
    public String toString() {
        return visible.toString();
    }
}
