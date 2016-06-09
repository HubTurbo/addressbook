package address.model;

import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.model.datatypes.tag.Tag;
import javafx.collections.ObservableList;

/**
 *
 */
public interface VisibleModel {

    /**
     * @return all persons in this visible model AS AN UNMODIFIABLE VIEW
     */
    ObservableList<ReadOnlyViewablePerson> getAllViewablePersonsAsObservable();

    /**
     * @return all persons in this visible model AS AN UNMODIFIABLE VIEW
     */
    ObservableList<ReadOnlyViewablePerson> getAllViewablePersonsAsReadOnly();
    /**
     * @return all tags in this visible model AS AN UNMODIFIABLE VIEW
     */
    ObservableList<Tag> getAllViewableTags();
}
