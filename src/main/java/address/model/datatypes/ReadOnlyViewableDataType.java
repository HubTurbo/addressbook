package address.model.datatypes;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;

/**
 * Session-lifetime data
 */
public interface ReadOnlyViewableDataType {

    ReadOnlyIntegerProperty secondsLeftInPendingStateProperty();
    int getSecondsLeftInPendingState();

    ReadOnlyBooleanProperty isEditedProperty();
    boolean isEdited();

    ReadOnlyBooleanProperty isDeletedProperty();
    boolean isDeleted();

    boolean isSyncingWithBackingObject();
}
