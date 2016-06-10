package address.model.datatypes;

import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.tag.Tag;
import address.util.collections.UnmodifiableObservableList;
import javafx.collections.ObservableList;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyAddressBook {

    /**
     * @return all persons in this model
     */
    UnmodifiableObservableList<ReadOnlyPerson> getAllPersonsReadOnly();

    /**
     * @return all tags in this model
     */
    UnmodifiableObservableList<Tag> getAllTagsReadOnly();
}
