package address.model.datatypes;

import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.model.datatypes.tag.Tag;
import address.util.collections.UnmodifiableObservableList;
import javafx.collections.ObservableList;

/**
 * Unmodifiable view of an address book containing viewable versions of domain objects
 */
public interface ReadOnlyViewableAddressBook extends ReadOnlyAddressBook {

    /**
     * @return all persons in this visible model AS AN UNMODIFIABLE VIEW
     */
    UnmodifiableObservableList<ReadOnlyViewablePerson> getAllViewablePersonsReadOnly();
    /**
     * @return all tags in this visible model AS AN UNMODIFIABLE VIEW
     */
    UnmodifiableObservableList<Tag> getAllViewableTagsReadOnly();
}
