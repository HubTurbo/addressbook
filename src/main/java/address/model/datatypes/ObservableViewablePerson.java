package address.model.datatypes;

import address.model.ModelManager;
import javafx.beans.property.IntegerProperty;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Same purpose as {@link ObservablePerson}, extended with additional status data viewable by user.
 */
public interface ObservableViewablePerson extends ObservablePerson, ReadableViewablePerson {

    /**
     * @param subtypeList source list of element type: subclasses of ObservableViewablePersons
     * @param collectionBuilder desired collection implementation of returned collection
     * @see ModelManager#upcastToBoundCollection(ObservableList, Supplier)
     * @return an upcasted read-only collection with element type {@code ObservableViewablePerson}
     */
    static <R extends Collection<ObservableViewablePerson>> R readOnlyCollectionCast(
            ObservableList<? extends ObservableViewablePerson> subtypeList, Supplier<R> collectionBuilder) {
        return ModelManager.upcastToBoundCollection(subtypeList, collectionBuilder);
    }

    IntegerProperty secondsLeftInPendingStateProperty();
}
