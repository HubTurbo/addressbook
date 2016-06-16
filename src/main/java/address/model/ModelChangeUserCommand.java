package address.model;

import address.model.datatypes.ViewableDataType;
import javafx.application.Platform;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static address.model.ModelChangeUserCommand.State.*;

/**
 * Framework-style superclass for all user commands that would cause a change in the model, and have it optimistically
 * reflected on the UI. Internal logic approximates a finite state machine. Abstract methods with return type
 * {@code State} are intended to implement state transition rules and command logic through side effects.
 *
 * Should be run OUTSIDE THE FX THREAD because the {@link #run()} method involves blocking calls.
 *
 * @param <I> command input type (most often a {@code ReadOnlyX})
 * @param <V> Viewable domain datatype used for optimistically simulating results
 */
public abstract class ModelChangeUserCommand<I, V extends ViewableDataType<?>> implements Runnable {

    public enum State {
        // Initial state
        NEWLY_CREATED,

        // Intermediate states
        RETRIEVING_INPUT,
        SIMULATING_RESULT,
        GRACE_PERIOD,
        CHECKING_AND_RESOLVING_REMOTE_CONFLICT,
        REQUESTING_REMOTE_CHANGE,

        // Terminal states
        CANCELLED, SUCCESSFUL, FAILED,
    }

    protected final int gracePeriodDurationInSeconds;

    // Alternate state transition path caused by an overriding command being given
    protected CompletableFuture<Supplier<State>> gracePeriodAlternateTransition;

    protected State state; // current state

    protected Supplier<I> inputSupplier;
    protected I input;
    protected V viewableItem;

    {
        state = NEWLY_CREATED;
        gracePeriodAlternateTransition = new CompletableFuture<>();
    }

    protected ModelChangeUserCommand(int gracePeriodDurationInSeconds) {
        this.gracePeriodDurationInSeconds = gracePeriodDurationInSeconds;
    }

    public State getState() {
        return state;
    }

    protected void clearGracePeriodAlternateTransition() {
        gracePeriodAlternateTransition = new CompletableFuture<>();
    }

    @Override
    public final void run() {
        // Runs FSM till one of terminal states is reached.
        while (!isTerminal(state)) {
            state = handleAndTransitionState(state);
        }
    }

    /**
     * @return true if {@code state} is one of the terminal states.
     */
    private boolean isTerminal(State state) {
        return state == CANCELLED || state == SUCCESSFUL || state == FAILED;
    }

    /**
     * Request to override this command with an edit command using {@code newInputSupplier} to supply input.
     * This is often called from outside the command's execution thread ({@link #run()}).
     * Only works if the command is currently in the {@link State#GRACE_PERIOD} state.
     *
     * @param newInputSupplier code to produce input for the overriding edit command
     */
    public synchronized void overrideWithEdit(Supplier<I> newInputSupplier) {
        if (state == GRACE_PERIOD) {
            gracePeriodAlternateTransition.complete(() -> handleEditInGracePeriod(newInputSupplier));
        }
    }

    /**
     * @see #overrideWithEdit(Supplier)
     */
    public synchronized void overrideWithDelete() {
        if (state == GRACE_PERIOD) {
            gracePeriodAlternateTransition.complete(this::handleDeleteInGracePeriod);
        }
    }

    /**
     * @see #overrideWithEdit(Supplier)
     */
    public synchronized void cancelCommand() {
        if (state == GRACE_PERIOD) {
            gracePeriodAlternateTransition.complete(this::handleCancel);
        }
    }

    /**
     * FSM engine to multiplex and run side-effect logic and determine next state
     * @param state state to be considered
     * @return next state
     */
    private State handleAndTransitionState(State state) {
        switch (state) {

        case RETRIEVING_INPUT :
            input = inputSupplier.get();
            return SIMULATING_RESULT;

        case SIMULATING_RESULT :
            assert input != null;
            simulateResult(input);
            return GRACE_PERIOD;

        case GRACE_PERIOD:
            assert viewableItem != null;
            return handleGracePeriodDelay();

        case CHECKING_AND_RESOLVING_REMOTE_CONFLICT :
            return checkAndHandleRemoteConflict();

        case REQUESTING_REMOTE_CHANGE :
            return requestChangeToRemote();

        case NEWLY_CREATED :
            return RETRIEVING_INPUT;

        default :
            assert false : "Implement handling for any new states!";
            throw new AssertionError("Incomplete implementation!");
        }
    }

    /**
     * Optimistically simulates the result of this command
     * @param input the request input supplied from the {@link State#RETRIEVING_INPUT} state
     */
    protected abstract void simulateResult(I input);

    /**
     * State transition for {@link State#GRACE_PERIOD}.
     *
     * Updates the countdown in {@link #viewableItem} every second.
     * Handles interruptions during the grace period that can change which state comes next.
     *
     * Since this command is tied to one {@link ViewableDataType}, there are 3 possible interruptions in this phase:
     *      - a request to cancel (undo) this command
     *      - a request to override this command into an edit command
     *      - a request to override this command into a delete command
     *
     * @see #cancelCommand()
     * @see #handleCancel()
     *
     * @see #overrideWithEdit(Supplier)
     * @see #handleEditInGracePeriod(Supplier)
     *
     * @see #overrideWithDelete()
     * @see #handleDeleteInGracePeriod()
     *
     * @return next state
     */
    private State handleGracePeriodDelay() {

        Platform.runLater(() -> viewableItem.setSecondsLeftInPendingState(gracePeriodDurationInSeconds));

        for (int i = gracePeriodDurationInSeconds; i > 0; i++) {
            try {
                // Overriding command issued during grace period; custom state transition
                Platform.runLater(() -> viewableItem.setSecondsLeftInPendingState(0));
                final State next = gracePeriodAlternateTransition.get(1, TimeUnit.SECONDS).get();
                clearGracePeriodAlternateTransition();
                return next;

            } catch (TimeoutException e) {
                ; // no edit/undo/delete interruption
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            Platform.runLater(viewableItem::decrementSecondsLeftInPendingState);
        }
        // default state transition path
        return CHECKING_AND_RESOLVING_REMOTE_CONFLICT;
    }

    /**
     * Called when an overriding edit request is made during this command's grace period.
     * Uses side-effects to implement logic, and returns next state for this command.
     *
     * @param newDataSupplier supplies data for the edit
     *
     * @see #handleGracePeriodDelay()
     * @see #overrideWithEdit(Supplier)
     *
     * @return next state (current state should be {@link State#GRACE_PERIOD}
     */
    protected abstract State handleEditInGracePeriod(Supplier<I> newDataSupplier);

    /**
     * @see #handleGracePeriodDelay()
     * @see #overrideWithDelete()
     * @see #handleEditInGracePeriod(Supplier)
     */
    protected abstract State handleDeleteInGracePeriod();

    /**
     * @see #handleGracePeriodDelay()
     * @see #cancelCommand()
     * @see #handleEditInGracePeriod(Supplier)
     */
    protected abstract State handleCancel();

    /**
     * State transition for {@link State#CHECKING_AND_RESOLVING_REMOTE_CONFLICT}
     *
     * Checks for any remote changes to this command's data object since this command was started,
     * then handles any conflicts found.
     *
     * @return next state
     */
    protected abstract State checkAndHandleRemoteConflict();

    /**
     * State transition for {@link State#REQUESTING_REMOTE_CHANGE}
     *
     * Sends a request for the change encapsulated by this command to be performed on the remote.
     * Handles the response from the remote server.
     *
     * @see State#SUCCESSFUL
     * @see State#FAILED
     * @return next state
     */
    protected abstract State requestChangeToRemote();
}
