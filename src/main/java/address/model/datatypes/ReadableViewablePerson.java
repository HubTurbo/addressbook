package address.model.datatypes;

import address.model.ModelManager;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Same purpose as {@link ReadablePerson}, extended with additional status data viewable by user.
 */
public interface ReadableViewablePerson extends ReadablePerson {

    int getSecondsLeftInPendingState();

    boolean isEdited();
    boolean isDeleted();
}
