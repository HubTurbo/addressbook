package address.model;

import address.model.datatypes.Person;
import address.model.datatypes.Tag;
import javafx.collections.ObservableList;

/**
 *
 */
public interface Model {

    /**
     * @return all persons in this model
     */
    ObservableList<Person> getAllPersons();

    /**
     * @return all tags in this model
     */
    ObservableList<Tag> getAllTags();
}
