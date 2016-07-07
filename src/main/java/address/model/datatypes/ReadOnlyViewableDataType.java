package address.model.datatypes;

import javafx.beans.property.ReadOnlyIntegerProperty;

/**
 * Session-lifetime data
 */
public interface ReadOnlyViewableDataType {

    ReadOnlyIntegerProperty secondsLeftInPendingStateProperty();
    int getSecondsLeftInPendingState();

    boolean isSyncingWithBackingObject();
}
