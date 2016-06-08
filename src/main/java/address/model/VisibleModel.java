package address.model;

import address.model.datatypes.ObservableViewablePerson;
import address.model.datatypes.Tag;
import javafx.collections.ObservableList;

/**
 *
 */
public interface VisibleModel {

    /**
     * @return all persons in this visible model
     */
    ObservableList<ObservableViewablePerson> getAllViewablePersons();

    /**
     * @return all tags in this visible model
     */
    ObservableList<Tag> getAllViewableTags();
}
