package address.model.datatypes.person;

import address.model.datatypes.UniqueData;
import address.model.datatypes.Viewable;
import address.model.datatypes.tag.Tag;
import address.util.collections.UnmodifiableObservableList;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ListChangeListener;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Extends {@link Viewable} for the Person domain object.
 * ID IS MUTABLE AND AFFECTS {@code .equals}, TAKE CARE WHEN USING THIS AS MAP KEY OR SET ELEMENT.
 * However, ID is functionally immutable after a backing Person exists.
 *
 * @see Viewable
 */
public class ViewablePerson extends Viewable<Person> implements ReadOnlyViewablePerson {

    private static final AtomicInteger tempIdCounter = new AtomicInteger(-1);
    private final List<Consumer<Integer>> remoteIdConfirmationHandlers;

    /**
     * can only be changed by {@link #connectBackingObject}
     */
    private int id;

    {
        remoteIdConfirmationHandlers = new ArrayList<>();
    }

    /**
     * Factory method: creates a new ViewablePerson using the argument Person as the backing object
     * @see super#Viewable(UniqueData, Function)
     */
    public static ViewablePerson fromBacking(Person backingPerson) {
        return new ViewablePerson(backingPerson, Person::new);
    }

    /**
     * @see #fromBacking(Person)
     * @see super#Viewable(UniqueData, Function)
     */
    private ViewablePerson(Person backingPerson, Function<Person, Person> visibleFactory) {
        super(backingPerson, visibleFactory);
        id = backingPerson.getId();
    }

    /**
     * Factory method: creates a new ViewablePerson using the argument Person as the visible object.
     * The returned ViewablePerson will have no backing person.
     *
     * @see super#Viewable(UniqueData)
     * @see #connectBackingObject(Person)
     */
    public static ViewablePerson withoutBacking(Person visiblePerson) {
        return new ViewablePerson(visiblePerson);
    }

    /**
     * @see #withoutBacking(Person)
     * @see super#Viewable(UniqueData)
     */
    private ViewablePerson(Person visiblePerson) {
        super(visiblePerson);
        assignTempId();
    }

    /**
     * Assigns a temporary ID for this viewableperson. Real IDs (remote-assigned) are positive integers, so temp IDs
     * are negative integers to avoid overlap.
     */
    private void assignTempId() {
        id = tempIdCounter.getAndDecrement();
    }

    @Override
    protected void conditionallyBindVisibleToBacking() {
        backing.forEachPropertyFieldPairWith(visible, this::conditionallyBindValue);

        // all changes here result in removal of affected list elements and (re)adding of any updated/new elements
        // at their correct positions.
        backing.getObservableTagList().addListener((ListChangeListener<? super Tag>) change -> {
            while (change.next()) {
                int from = change.getFrom();
                int to = change.getTo();
                if (change.wasPermutated()) { // element reordering
                    visible.getTags().subList(from, to).clear();
                    visible.getTags().addAll(from, backing.getObservableTagList().subList(from, to));
                } else { // list/element mutation
                    visible.getTags().subList(from, from + change.getRemovedSize()).clear();
                    visible.getTags().addAll(from,
                            backing.getObservableTagList().subList(from, from + change.getAddedSize()));
                }
            }
        });
        forceSyncFromBacking();
    }

    @Override
    public void forceSyncFromBacking() {
        visible.update(backing);
    }

    @Override
    public void connectBackingObject(Person backingPerson) {
        if (backingPerson == null) {
            throw new NullPointerException();
        }
        if (backing != null) {
            throw new IllegalStateException("Cannot override backing object");
        }
        id = backingPerson.getId();
        remoteIdConfirmationHandlers.forEach(cb -> cb.accept(id));
        remoteIdConfirmationHandlers.clear();

        backing = backingPerson;
        conditionallyBindVisibleToBacking();
        isSyncingWithBackingObject = true;
        forceSyncFromBacking();
    }

//// OPTIMISTIC UPDATING

    /**
     * Updates the visible data, backing data remains untouched.
     */
    public void simulateUpdate(ReadOnlyPerson data) {
        visible.update(data);
    }

    @Override
    public void onRemoteIdConfirmed(Consumer<Integer> callback) {
        if (existsOnRemote()) {
            callback.accept(id);
        } else {
            remoteIdConfirmationHandlers.add(callback);
        }
    }

//// PERSON ACCESSORS

    @Override
    public int getId() {
        return id;
    }

    @Override
    public ReadOnlyStringProperty firstNameProperty() {
        return visible.firstNameProperty();
    }

    @Override
    public ReadOnlyStringProperty lastNameProperty() {
        return visible.lastNameProperty();
    }

    public ReadOnlyStringProperty githubUsernameProperty() {
        return visible.githubUsernameProperty();
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
    public String getGithubUsername() {
        return visible.getGithubUsername();
    }

    @Override
    public URL profilePageUrl() {
        try {
            return new URL("https://github.com/" + getGithubUsername());
        } catch (MalformedURLException e) {
            try {
                return new URL("https://github.com");
            } catch (MalformedURLException e1) {
                assert false;
            }
        }
        return null;
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
    public List<Tag> getTagList() {
        return visible.getTagList();
    }

    @Override
    public UnmodifiableObservableList<Tag> getObservableTagList() {
        return visible.getObservableTagList();
    }

    @Override
    public String tagsString() {
        return visible.tagsString();
    }

    @Override
    public boolean isSameName(String firstName, String lastName) {
        return this.firstNameProperty().getValue().equals(firstName)
                && this.lastNameProperty().getValue().equals(lastName);
    }

    /**
     * Use backing Person for comparison.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (ViewablePerson.class.isAssignableFrom(other.getClass())) {
            final ViewablePerson otherVP = (ViewablePerson) other;
            return this.getId() == otherVP.getId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getId();
    }

    @Override
    public String toString() {
        return backing != null ? backing.toString() : "Pending Person: " + visible.fullName();
    }
}
