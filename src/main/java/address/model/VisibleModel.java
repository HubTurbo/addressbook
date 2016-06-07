package address.model;

import address.model.datatypes.Tag;
import address.model.datatypes.ViewablePerson;
import javafx.collections.ObservableList;

/**
 *
 */
public interface VisibleModel {

    /**
     * @return all persons in this visible model
     */
    ObservableList<ViewablePerson> getAllViewablePersons();

    /**
     * @return all tags in this visible model
     */
    ObservableList<Tag> getAllViewableTags();
}
