package address.model.datatypes;

import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.model.datatypes.tag.Tag;
import javafx.collections.ObservableList;

/**
 * Unmodifiable view of an address book containing viewable versions of domain objects
 */
public interface ReadOnlyViewableAddressBook extends ReadOnlyAddressBook {

    /**
     * @return all persons in this visible model AS AN UNMODIFIABLE VIEW
     */
    ObservableList<ReadOnlyViewablePerson> getAllViewablePersonsReadOnly();
    /**
     * @return all tags in this visible model AS AN UNMODIFIABLE VIEW
     */
    ObservableList<Tag> getAllViewableTagsReadOnly();
}
