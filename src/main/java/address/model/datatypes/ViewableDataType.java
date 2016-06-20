package address.model.datatypes;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;

import java.util.function.Function;

/**
 * Common functionality among all ViewableX classes:
 * eg.  - the ability to deactivate and reactivate syncing visible data with backing data (for optimistic ui updating)
 *      - common fields/references and constructor logic
 *      - maintains visible state separately from canonical state (see {@link #visible}, {@link #backing})
 *      - common application session-lifecycle state across all domain data objects
 *
 * Note: these status data fields should not be directly edited by users.
 */
public abstract class ViewableDataType<D extends UniqueData> extends UniqueData implements ReadOnlyViewableDataType {

    protected final D visible;
    protected D backing;
    protected boolean isSyncingWithBackingObject;

    protected final IntegerProperty secondsLeftInPendingState; // Negative when not in pending state
    protected final BooleanProperty isDeleted;
    protected final BooleanProperty isEdited;

    {
        secondsLeftInPendingState = new SimpleIntegerProperty(-1);
        isDeleted = new SimpleBooleanProperty(false);
        isEdited = new SimpleBooleanProperty(false);
    }

    /**
     * Create a new ViewableDataType based on a backing object.
     * @param backingObject used as {@link #backing}.
     * @param visibleObjectGenerator used to generate {@link #visible} with {@code backingObject} as the argument.
     */
    protected ViewableDataType(D backingObject, Function<D, D> visibleObjectGenerator) {
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
     * @param listener will be updated whenever {@code dependency} changes
     *                 AND @link #isSyncingWithBackingObject} is true.
     * @param <T> ensures both arguments have same param type.
     */
    protected <T> void conditionallyBindValue(ObservableValue<T> dependency, WritableValue<T> listener) {
        dependency.addListener((dep, oldValue, newValue) -> {
            if (this.isSyncingWithBackingObject) {
                listener.setValue(newValue);
            }
        });
    }


    public D getVisible() {
        return visible;
    }

    public D getBacking() {
        return backing;
    }


// APPLICATION STATE ACCESSORS

    @Override
    public ReadOnlyIntegerProperty secondsLeftInPendingStateProperty() {
        return secondsLeftInPendingState;
    }

    @Override
    public int getSecondsLeftInPendingState() {
        return secondsLeftInPendingState.get();
    }

    public void setSecondsLeftInPendingState(int s) {
        secondsLeftInPendingState.set(s);
    }

    public void decrementSecondsLeftInPendingState() {
        secondsLeftInPendingState.set(secondsLeftInPendingState.get() - 1);
    }

    @Override
    public boolean isDeleted() {
        return isDeleted.get();
    }

    @Override
    public ReadOnlyBooleanProperty isDeletedProperty() {
        return isDeleted;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted.set(isDeleted);
    }

    @Override
    public boolean isEdited() {
        return isEdited.get();
    }

    @Override
    public ReadOnlyBooleanProperty isEditedProperty() {
        return isEdited;
    }

    public void setIsEdited(boolean isEdited) {
        this.isEdited.set(isEdited);
    }


// VISIBLE--BACKING binding controls

    /**
     * @return true if changes to the backing object AFTER this method call will propagate to the visible object.
     */
    @Override
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
