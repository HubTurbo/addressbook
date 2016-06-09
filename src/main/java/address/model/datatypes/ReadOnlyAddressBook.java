package address.model.datatypes;

import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.tag.Tag;
import javafx.collections.ObservableList;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyAddressBook {

    /**
     * @return all persons in this model
     */
    ObservableList<ReadOnlyPerson> getAllPersonsReadOnly();

    /**
     * @return all tags in this model
     */
    ObservableList<Tag> getAllTagsReadOnly();
}
