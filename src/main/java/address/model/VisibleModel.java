package address.model;

import address.model.datatypes.person.ObservableViewablePerson;
import address.model.datatypes.person.ReadableViewablePerson;
import address.model.datatypes.tag.Tag;
import javafx.collections.ObservableList;

/**
 *
 */
public interface VisibleModel {

    /**
     * @return all persons in this visible model AS AN UNMODIFIABLE VIEW
     */
    ObservableList<ObservableViewablePerson> getAllViewablePersonsAsObservable();

    /**
     * @return all persons in this visible model AS AN UNMODIFIABLE VIEW
     */
    ObservableList<ReadableViewablePerson> getAllViewablePersonsAsReadOnly();
    /**
     * @return all tags in this visible model AS AN UNMODIFIABLE VIEW
     */
    ObservableList<Tag> getAllViewableTags();
}
