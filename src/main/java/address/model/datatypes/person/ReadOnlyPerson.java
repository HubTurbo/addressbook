package address.model.datatypes.person;

import address.model.datatypes.ExtractableObservables;
import address.model.datatypes.tag.Tag;
import address.util.DateTimeUtil;
import address.util.collections.UnmodifiableObservableList;
import com.teamdev.jxbrowser.chromium.internal.URLUtil;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Allows read-only access to the Person domain object's data.
 */
public interface ReadOnlyPerson extends ExtractableObservables {

    /**
     * @see Collection#removeAll(Collection)
     * @see #removeAllUsingIDs(Collection, Collection)
     * @param col collection to remove from
     * @param toRemove collection of ReadOnlyPersons with the IDs of the those you wish to remove from {@code col}
     * @return whether {@code col} was changed as a result of this operation
     */
    static boolean removeAll(Collection<? extends ReadOnlyPerson> col,
                             Collection<? extends ReadOnlyPerson> toRemove) {
        return removeAllUsingIDs(col, toRemove.stream().map(e -> e.getId()).collect(Collectors.toList()));
    }

    /**
     * @see Collection#removeAll(Collection)
     * @param col collection to remove from
     * @param idsToRemove collection of IDs of the persons you wish to remove from {@code col}
     * @return whether {@code col} was changed as a result of this operation
     */
    static boolean removeAllUsingIDs(Collection<? extends ReadOnlyPerson> col,
                                     Collection<Integer> idsToRemove) {
        final Set<Integer> idSet = new HashSet<>(idsToRemove);
        final Iterator<? extends ReadOnlyPerson> iter = col.iterator();
        boolean changed = false;
        while (iter.hasNext()) {
            if (idSet.contains(iter.next().getId())) {
                iter.remove();
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Remote-assigned (canonical) ids are positive integers.
     * Locally-assigned temporary ids are negative integers.
     * 0 is reserved for ID-less Person data containers.
     */
    int getId();
    default String idString() {
        return hasConfirmedRemoteID() ? "#" + getId() : "#TBD";
    }
    /**
     * @see #getId()
     */
    default boolean hasConfirmedRemoteID() {
        return getId() > 0;
    }

    String getFirstName();
    String getLastName();
    /**
     * @return first-last format full name
     */
    default String fullName() {
        return getFirstName() + ' ' + getLastName();
    }

    String getGithubUsername();

    default URL profilePageUrl(){
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
    default Optional<String> githubProfilePicUrl() {
        if (getGithubUsername().length() > 0) {
            String profilePicUrl = profilePageUrl().toExternalForm() + ".png";
            if (URLUtil.isURIFormat(profilePicUrl)){
                return Optional.of(profilePicUrl);
            }
        }
        return Optional.empty();
    }

    String getStreet();
    String getPostalCode();
    String getCity();

    LocalDate getBirthday();
    /**
     * @return birthday date-formatted as string
     */
    default String birthdayString() {
        if (getBirthday() == null) return "";
        return DateTimeUtil.format(getBirthday());
    }

    /**
     * @return unmodifiable list view of tags.
     */
    List<Tag> getTagList();
    /**
     * @return string representation of this Person's tags
     */
    default String tagsString() {
        final StringBuffer buffer = new StringBuffer();
        final String separator = ", ";
        getTagList().forEach(tag -> buffer.append(tag).append(separator));
        if (buffer.length() == 0) {
            return "";
        } else {
            return buffer.substring(0, buffer.length() - separator.length());
        }
    }

//// Operations below are optional; override if they will be needed.

    default ReadOnlyStringProperty firstNameProperty() {
        throw new UnsupportedOperationException();
    }
    default ReadOnlyStringProperty lastNameProperty() {
        throw new UnsupportedOperationException();
    }
    default ReadOnlyStringProperty githubUsernameProperty() {
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
                githubUsernameProperty(),

                streetProperty(),
                postalCodeProperty(),
                cityProperty(),

                birthdayProperty(),
                getObservableTagList()
        };
    }

    static List<Tag> getSelectedPersonsAssignedTag(List<ReadOnlyViewablePerson> persons) {
        Set<Tag> tags = new HashSet<>();
        persons.stream().forEach(p -> tags.addAll(p.getObservableTagList()));
        List<Tag> assignedTags = tags.stream().filter(tag ->
                persons.stream()
                        .filter(p -> p.getObservableTagList().contains(tag))
                        .count() == persons.size())
                .collect(Collectors.toCollection(ArrayList::new));
        return assignedTags;
    }
}
