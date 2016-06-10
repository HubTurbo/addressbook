package address.model.datatypes.person;

import address.model.datatypes.ReadOnlyViewableDataType;
import javafx.beans.Observable;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Same purpose as {@link ReadOnlyPerson}, extended with additional Person status data viewable by user.
 */
public interface ReadOnlyViewablePerson extends ReadOnlyPerson, ReadOnlyViewableDataType {

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
