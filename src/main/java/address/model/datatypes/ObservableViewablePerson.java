package address.model.datatypes;

import address.model.ModelManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Same purpose as {@link ObservablePerson}, extended with additional status data viewable by user.
 */
public interface ObservableViewablePerson extends ObservablePerson, ReadableViewablePerson {

    IntegerProperty secondsLeftInPendingStateProperty();

    BooleanProperty isEditedProperty();
    BooleanProperty isDeletedProperty();
}
