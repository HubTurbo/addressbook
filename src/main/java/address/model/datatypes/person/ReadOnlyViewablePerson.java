package address.model.datatypes.person;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Same purpose as {@link ReadOnlyPerson}, extended with additional status data viewable by user.
 */
public interface ReadOnlyViewablePerson extends ReadOnlyPerson {

    int getSecondsLeftInPendingState();
    boolean isEdited();
    boolean isDeleted();

    ReadOnlyIntegerProperty secondsLeftInPendingStateProperty();
    ReadOnlyBooleanProperty isEditedProperty();
    ReadOnlyBooleanProperty isDeletedProperty();

    @Override
    default Observable[] extractObservables() {
        final Observable[] obs = {
                secondsLeftInPendingStateProperty(),
                isEditedProperty(),
                isDeletedProperty()
        };
        return Stream.concat(
                Arrays.stream(ReadOnlyPerson.super.extractObservables()),
                Arrays.stream(obs))
                .toArray(Observable[]::new);
    }
}
