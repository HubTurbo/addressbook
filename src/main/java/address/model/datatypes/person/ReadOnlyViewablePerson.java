package address.model.datatypes.person;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Same purpose as {@link ReadOnlyPerson}, extended with additional status data viewable by user.
 */
public interface ReadOnlyViewablePerson extends ReadOnlyPerson {

    int getSecondsLeftInPendingState();
    boolean isEdited();
    boolean isDeleted();

    IntegerProperty secondsLeftInPendingStateProperty();
    BooleanProperty isEditedProperty();
    BooleanProperty isDeletedProperty();

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
