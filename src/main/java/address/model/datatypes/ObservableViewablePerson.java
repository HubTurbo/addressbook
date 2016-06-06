package address.model.datatypes;

import javafx.beans.property.IntegerProperty;

/**
 * Same purpose as {@link ObservablePerson}, extended with additional status data viewable by user.
 */
public interface ObservableViewablePerson extends ObservablePerson, ReadableViewablePerson {

    IntegerProperty secondsLeftInPendingStateProperty();
}
