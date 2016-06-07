package address.model.datatypes;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;

import java.util.function.Function;

/**
 * Common functionality among all ViewableX classes:
 * eg.  - the ability to deactivate and reactivate syncing visible data with backing data (for optimistic ui updating)
 *      - common fields/references and constructor logic
 *      - maintains visible state separately from canonical state (see {@link #visible}, {@link #backing})
 */
public abstract class Viewable<D extends BaseDataType> extends UniqueData {

    protected final D visible;
    protected D backing;
    protected boolean isSyncingWithBackingObject;

    /**
     * Negative when nothing is pending, else equal to seconds left before pending period ends.
     * Anyone who wants to be updated on the countdown at the second level needs only to observe this property.
     */
    protected final IntegerProperty secondsLeftInPendingState;

    {
        secondsLeftInPendingState = new SimpleIntegerProperty(-1);
    }

    /**
     * Create a new Viewable based on a backing object.
     * @param backingObject used as {@link #backing}.
     * @param visibleObjectGenerator used to generate {@link #visible} with {@code backingObject} as the argument.
     */
    protected Viewable(D backingObject, Function<D, D> visibleObjectGenerator) {
        backing = backingObject;
        visible = visibleObjectGenerator.apply(backingObject);
        conditionallyBindVisibleToBacking();
        isSyncingWithBackingObject = true;
    }

    /**
     * Make every relevant data field inside the {@link #visible} object track and mirror the {@link #backing} object,
     * as long as {@link #isSyncingWithBackingObject} is true.
     *
     * Consider using the {@link #conditionallyBindValue(ObservableValue, WritableValue)} helper method to make
     * changes to {@code ObservableValue} fields in {@link #backing} propagate to {@code WritableValue} fields in
     * {@link #visible} only when {@link #isSyncingWithBackingObject} is true.
     *
     * @see #conditionallyBindValue(ObservableValue, WritableValue)
     * @see #visible
     * @see #backing
     */
    protected abstract void conditionallyBindVisibleToBacking();

    /**
     * Binds {@code listener} to {@code dependency} such that ONLY whenever {@code dependency} changes AND
     * {@link #isSyncingWithBackingObject} is true, then {@code listener} gets updated.
     *
     * @param dependency notifies {@code listener} of any changes.
     * @param listener will be updated whenever {@code dependency} changes AND {@link #isSyncingWithBackingObject} is true.
     * @param <T> ensures both arguments have same type parameter.
     */
    protected <T> void conditionallyBindValue(ObservableValue<T> dependency, WritableValue<T> listener) {
        dependency.addListener((dep, oldValue, newValue) -> {
            if (this.isSyncingWithBackingObject) {
                listener.setValue(newValue);
            }
        });
    }

// APPLICATION STATE ACCESSORS


    public IntegerProperty secondsLeftInPendingStateProperty() {
        return secondsLeftInPendingState;
    }

    public int getSecondsLeftInPendingState() {
        return secondsLeftInPendingState.get();
    }


// VISIBLE--BACKING binding controls

    /**
     * @return true if changes to the backing object AFTER this method call will propagate to the visible object.
     */
    public boolean isSyncingWithBackingObject() {
        return isSyncingWithBackingObject;
    }

    /**
     * Changes in the backing object will be stored but not visible.
     */
    public void stopSyncingWithBackingObject() {
        isSyncingWithBackingObject = false;
    }

    /**
     * Future changes to the backing object will propagate to the visible object.
     * NOTE: If backing and visible states have diverged, will NOT force a convergence.
     *       Each visible object's field will only converge when the corresponding backing object field is changed.
     */
    public void continueSyncingWithBackingObject() {
        isSyncingWithBackingObject = true;
    }

    /**
     * Forcibly updates visible state to match current backing state
     */
    public abstract void forceSyncFromBacking();

}
