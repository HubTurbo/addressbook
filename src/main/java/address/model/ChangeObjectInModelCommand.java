package address.model;

import address.util.AppLogger;
import address.util.LoggerManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.concurrent.*;
import java.util.function.Supplier;

import static address.model.ChangeObjectInModelCommand.State.*;

/**
 * Framework-style superclass for all commands that would cause changes for single domain objects in the model,
 * and have it optimistically reflected on the UI. Internal logic approximates a finite state machine.
 * Abstract methods with return type  {@code State} are intended to implement state transition rules and command logic
 * through side effects.
 *
 * Should be run OUTSIDE THE FX THREAD because the {@link #run()} method involves blocking calls.
 */
public abstract class ChangeObjectInModelCommand implements Runnable {

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

    private static final AppLogger logger = LoggerManager.getLogger(ChangeObjectInModelCommand.class);

    private final CountDownLatch completionLatch;
    protected final int gracePeriodDurationInSeconds;
    protected final ObjectProperty<State> state; // current state

    // Alternate state transition path caused by an interrupting signal during the grace period
    private CompletableFuture<Supplier<State>> overrideGracePeriod;
    
    {
        completionLatch = new CountDownLatch(1);
        state = new SimpleObjectProperty<>(NEWLY_CREATED);
        clearGracePeriodOverride();
    }

    protected ChangeObjectInModelCommand(int gracePeriodDurationInSeconds) {
        this.gracePeriodDurationInSeconds = gracePeriodDurationInSeconds;
    }

    public void waitForCompletion() throws InterruptedException {
        completionLatch.await();
    }

    public State getState() {
        return state.getValue();
    }

    void setState(State newState) {
        state.setValue(newState);
    }

    public ReadOnlyObjectProperty<State> stateProperty() {
        return state;
    }

    /**
     * @see #countdownGracePeriodAndHandleOverrides()
     * @param alternateStateTransition will override the default grace period state transition
     */
    protected void signalGracePeriodOverride(Supplier<State> alternateStateTransition) {
        if (getState() != GRACE_PERIOD) {
            LoggerManager.getLogger(this.getClass()).warn("Overriding signal received outside of grace period");
        }
        final boolean hasExistingValue = !overrideGracePeriod.complete(alternateStateTransition);
        if (hasExistingValue) {
            overrideGracePeriod.obtrudeValue(alternateStateTransition);
        }
    }

    /**
     * Clears any grace period overriding state transition functions such that
     * {@link #countdownGracePeriodAndHandleOverrides()} will have to wait for any new overrides instead of using the
     * previously existing override.
     *
     * @see #signalGracePeriodOverride(Supplier)
     */
    protected void clearGracePeriodOverride() {
        overrideGracePeriod = new CompletableFuture<>();
    }

    @Override
    public final void run() {
        before();

        // Runs FSM till one of terminal states is reached.
        while (!isTerminal(getState())) {
            logger.info("HandleAndTransitionState before: " + getState().toString());
            setState(handleAndTransitionState(getState()));
            logger.info("HandleAndTransitionState after: " + getState().toString());
        }

        logger.info("Reached terminal state " + getState().toString());

        // handle terminal states
        switch (getState()) {
        case CANCELLED :
            finishWithCancel();
            break;
        case SUCCESSFUL :
            finishWithSuccess();
            break;
        case FAILED :
            finishWithFailure();
            break;
        default :
            assert false;
        }
        after();
        completionLatch.countDown();
    }

    /**
     * Setup hook (similar concept to junit {@code @Before})
     * Called first when this command starts running.
     */
    protected abstract void before();

    /**
     * Cleanup hook (similar concept to junit {@code @After})
     * Called last after this command hits a terminal state and right before it stops.
     */
    protected abstract void after();

    /**
     * Runs when the terminal {@link State#CANCELLED} state is reached.
     */
    protected abstract void finishWithCancel();

    /**
     * Runs when the terminal {@link State#SUCCESSFUL} state is reached.
     */
    protected abstract void finishWithSuccess();

    /**
     * Runs when the terminal {@link State#FAILED} state is reached.
     */
    protected abstract void finishWithFailure();

    /**
     * @return true if {@code state} is one of the terminal states.
     */
    private boolean isTerminal(State state) {
        return state == CANCELLED || state == SUCCESSFUL || state == FAILED;
    }

    /**
     * FSM engine to multiplex and run side-effect logic and determine next state
     * @param state state to be considered
     * @return next state
     */
    private State handleAndTransitionState(State state) {
        switch (state) {

        case RETRIEVING_INPUT :
            return retrieveInput();

        case SIMULATING_RESULT :
            return simulateResult();

        case GRACE_PERIOD:
            return countdownGracePeriodAndHandleOverrides();

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
     * Retrieves and stores command input via side effects.
     * @return next state
     */
    protected abstract State retrieveInput();

    /**
     * Optimistically simulates the result of this command.
     * @return next state
     */
    protected abstract State simulateResult();

    /**
     * State transition for {@link State#GRACE_PERIOD}.
     *
     * Updates {@link #handleChangeToSecondsLeftInGracePeriod(int)} whenever seconds remaining in the
     * grace period countdown changes.
     *
     * This grace period phase allows signals to interrupt and override the execution and state transition path
     * Partial support is offered for a 'cancel' signal.
     * Some examples:
     *      - a request to cancel and undo this command ({@link #cancelInGracePeriod()})
     *      - a request to override this command with another command (eg. edit/delete)
     * Such interruptions can change the execution path and which state comes next.
     *
     *
     * @see #signalGracePeriodOverride(Supplier)
     * @see #cancelInGracePeriod()
     * @see #handleCancelInGracePeriod()
     *
     * @return next state
     */
    private State countdownGracePeriodAndHandleOverrides() {

        beforeGracePeriod();
        // Ensure that any override signals detected happen during the current grace period.
        clearGracePeriodOverride();

        // Countdown loop that listens for any overriding signals
        for (int i = gracePeriodDurationInSeconds; i > 0; i--) {
            handleChangeToSecondsLeftInGracePeriod(i);

            try {
                // Wait for overriding signal issued during grace period; custom state transition
                final State next = overrideGracePeriod.get(1, TimeUnit.SECONDS).get();
                overrideGracePeriod = new CompletableFuture<>();
                handleChangeToSecondsLeftInGracePeriod(0); // signify end of grace period
                return next;

            } catch (TimeoutException e) {
                // no overriding signal this past second
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        handleChangeToSecondsLeftInGracePeriod(0);
        // default state transition
        return CHECKING_AND_RESOLVING_REMOTE_CONFLICT;
    }

    /**
     * Hook that runs when the state enters {@link State#GRACE_PERIOD} right before the countdown starts.
     */
    protected abstract void beforeGracePeriod();

    /**
     * Hook that gets called every time number of seconds left in the grace period changes.
     * @see #countdownGracePeriodAndHandleOverrides()
     */
    protected abstract void handleChangeToSecondsLeftInGracePeriod(int secondsLeft);

    /**
     * Request to cancel this command. Only works if called while command is in the grace period state.
     *
     * @see #handleCancelInGracePeriod()
     * @see #countdownGracePeriodAndHandleOverrides()
     */
    synchronized void cancelInGracePeriod() {
        signalGracePeriodOverride(this::handleCancelInGracePeriod);
    }


    /**
     * When the 'cancel' overriding signal is received (from {@link #cancelInGracePeriod()}) during the grace period,
     * this method takes over the state transition path and execution.
     * Will be called from the {@link #run()} thread.
     *
     * @see #countdownGracePeriodAndHandleOverrides()
     * @see #cancelInGracePeriod()
     * @see State#CANCELLED
     * @return next state
     */
    protected abstract State handleCancelInGracePeriod();

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
