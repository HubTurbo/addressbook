package address.model.datatypes.person;

import address.model.datatypes.ReadOnlyViewableDataType;
import javafx.beans.Observable;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Same purpose as {@link ReadOnlyPerson}, extended with additional Person status data viewable by user.
 */
public interface ReadOnlyViewablePerson extends ReadOnlyPerson, ReadOnlyViewableDataType {

    /**
     * @return whether this person exists on the remote server
     * @see #hasConfirmedRemoteID()
     */
    default boolean existsOnRemote() {
        return hasConfirmedRemoteID();
    }

    /**
     * If remote id has already been confirmed (this person exists on remote server), runs callback immediately.
     *
     * If remote id not yet confirmed (optimistic simulation of {@code }ADD NEW PERSON]), then runs callback
     * when remote id is confirmed. Automatically unregisters the callback listener after it is run.
     *
     * @see #existsOnRemote()
     * @param callback takes the confirmed remote id as the sole argument
     */
    void onRemoteIdConfirmed(Consumer<Integer> callback);

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

    boolean isSameName(String firstName, String lastName);

}
