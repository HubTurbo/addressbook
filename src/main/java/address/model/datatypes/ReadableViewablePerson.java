package address.model.datatypes;

import address.model.ModelManager;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Same purpose as {@link ReadablePerson}, extended with additional status data viewable by user.
 */
public interface ReadableViewablePerson extends ReadablePerson {

    /**
     * @param subtypeList source list of element type: subclasses of ReadableViewablePersons
     * @param collectionBuilder desired collection implementation of returned collection
     * @see ModelManager#upcastToBoundCollection(ObservableList, Supplier)
     * @return an upcasted read-only collection with element type {@code ReadableViewablePerson}
     */
    static <R extends Collection<ReadableViewablePerson>> R readOnlyCollectionCast(
            ObservableList<? extends ReadableViewablePerson> subtypeList, Supplier<R> collectionBuilder) {
        return ModelManager.upcastToBoundCollection(subtypeList, collectionBuilder);
    }


    int getSecondsLeftInPendingState();
}
