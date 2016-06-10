package address.model.datatypes.person;

import address.model.datatypes.ViewableDataType;
import address.model.datatypes.tag.Tag;
import address.util.collections.UnmodifiableObservableList;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Extends {@link ViewableDataType} for the Person domain object.
 * @see ViewableDataType
 */
public class ViewablePerson extends ViewableDataType<Person> implements ReadOnlyViewablePerson {


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


// PERSON ACCESSORS

    @Override
    public ReadOnlyStringProperty firstNameProperty() {
        return visible.firstNameProperty();
    }

    @Override
    public ReadOnlyStringProperty lastNameProperty() {
        return visible.lastNameProperty();
    }

    @Override
    public ReadOnlyStringProperty githubUserNameProperty() {
        return visible.githubUserNameProperty();
    }

    @Override
    public ReadOnlyStringProperty streetProperty() {
        return visible.streetProperty();
    }

    @Override
    public ReadOnlyStringProperty postalCodeProperty() {
        return visible.postalCodeProperty();
    }

    @Override
    public ReadOnlyStringProperty cityProperty() {
        return visible.cityProperty();
    }

    @Override
    public ReadOnlyObjectProperty<LocalDate> birthdayProperty() {
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
    public URL profilePageUrl() {
        URL url = null;

        try {
            url = new URL("https://github.com/" + getGithubUserName());
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
        return visible.githubProfilePicUrl();
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
    public UnmodifiableObservableList<Tag> getTags() {
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
