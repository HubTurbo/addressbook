package address.model;

import address.util.AppLogger;
import address.util.LoggerManager;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Optional;
import java.util.concurrent.*;

import static address.model.ChangeObjectInModelCommand.State.*;
import static address.model.SingleTargetCommandResult.CommandStatus;

/**
 * Framework-style superclass for all commands that would cause changes for single domain objects in the model,
 * and have it optimistically reflected on the UI. Internal logic approximates a finite state machine.
 *
 * Should be run OUTSIDE THE FX THREAD because the {@link #run()} method involves blocking calls.
 */
public abstract class ChangeObjectInModelCommand implements Runnable {

    public enum State {
        // Initial state
        NEWLY_CREATED               ("Newly Created"),

        // Intermediate states
        RETRIEVING_INPUT            ("Retrieving Input"),
        SIMULATING_RESULT           ("Optimistically Simulating Result"),
        GRACE_PERIOD                ("Pending / Grace Period"),
        CHECKING_REMOTE_CONFLICT    ("Checking Remote for Unseen Changes"),
        REQUESTING_REMOTE_CHANGE    ("Requesting Change to Remote"),

        // Requires user intervention
        CONFLICT_FOUND              ("Conflict on Remote"),
        REQUEST_FAILED              ("Remote Request Failed"),

        // Terminal states
        CANCELLED                   ("Cancelled", CommandStatus.CANCELLED),
        SUCCESSFUL                  ("Successful", CommandStatus.SUCCESSFUL);

        private final String descr;
        private final Optional<CommandStatus> resultStatusMapping;

        State(String descr) {
            this.descr = descr;
            resultStatusMapping = Optional.empty();
        }
        State(String descr, CommandStatus resultStatusMapping) {
            this.descr = descr;
            this.resultStatusMapping = Optional.of(resultStatusMapping);
        }

        @Override
        public String toString() {
            return descr;
        }

        public CommandStatus toResultStatus() {
            if (resultStatusMapping.isPresent()) {
                return resultStatusMapping.get();
            } else {
                throw new UnsupportedOperationException();
            }
        }

        /**
         * @return true if this is one of the terminal states.
         */
        public boolean isTerminal() {
            return this == CANCELLED || this == SUCCESSFUL;
        }
    }

    protected final AppLogger logger = LoggerManager.getLogger(this.getClass());

    private final int commandId;

    protected final int gracePeriodDurationInSeconds;
    protected final Property<State> state; // current state

    private final CountDownLatch completionLatch; // blocking completion flag
    protected final CountDownLatch cancelledLatch; // blocking cancellation flag
    
    {
        completionLatch = new CountDownLatch(1); // irreversible flag
        cancelledLatch = new CountDownLatch(1); // irreversible flag
        state = new SimpleObjectProperty<>(NEWLY_CREATED);
    }

    protected ChangeObjectInModelCommand(int commandId, int gracePeriodDurationInSeconds) {
        this.commandId = commandId;
        this.gracePeriodDurationInSeconds = gracePeriodDurationInSeconds;
    }

    public int getCommandId() {
        return commandId;
    }

    /**
     * Blocks until this command finishes execution by reaching terminal state.
     */
    public void waitForCompletion() throws InterruptedException {
        completionLatch.await();
    }

    /**
     * Request to cancel this command.
     */
    public void cancelCommand() {
        cancelledLatch.countDown();
    }

    /**
     * Blocks till a Cancel Command request is received.
     * @see CountDownLatch#await()
     */
    protected void waitForCancelRequest() throws InterruptedException {
        cancelledLatch.await();
    }

    /**
     * Blocks for the specified amount of time, or till a Cancel Command request is received.
     * @see CountDownLatch#await(long, TimeUnit)
     * @return true if cancel request has been received, false if timeout
     */
    protected boolean waitForCancelRequest(long timeout, TimeUnit unit) throws InterruptedException {
        return cancelledLatch.await(timeout, unit);
    }

    public State getState() {
        return state.getValue();
    }

    void setState(State newState) {
        state.setValue(newState);
    }

    /**
     * Hook that runs before new states are handled
     */
    protected void beforeState(State currentState) {
        // override to inject code
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

    @Override
    public final void run() {
        before();

        // Runs FSM till one of terminal states is reached.
        while (!getState().isTerminal()) {
            logger.debug("Handling state: " + getState().name());
            setState(handleAndTransitionState(getState()));
        }

        logger.debug("Reached terminal state " + getState().name());

        // handle terminal states
        beforeState(getState());
        switch (getState()) {
            case CANCELLED :
                finishWithCancel();
                break;
            case SUCCESSFUL :
                finishWithSuccess();
                break;
            default :
                assert false;
        }

        after();
        completionLatch.countDown();
    }

    /**
     * FSM engine to multiplex and run side-effect logic transition state
     * @param state state to be considered
     * @return next state
     */
    private State handleAndTransitionState(State state) {
        beforeState(state);
        switch (state) {
        case NEWLY_CREATED :
            return RETRIEVING_INPUT;

        case RETRIEVING_INPUT :
            return retrieveValidInput() ?
                    SIMULATING_RESULT : CANCELLED;

        case SIMULATING_RESULT :
            simulateResult();
            return GRACE_PERIOD;

        case GRACE_PERIOD:
            return gracePeriodCountdownAndTransition();

        case CHECKING_REMOTE_CONFLICT:
            return checkForRemoteConflict() ?
                    CONFLICT_FOUND : REQUESTING_REMOTE_CHANGE;

        case CONFLICT_FOUND:
            handleRemoteConflict();
            return CANCELLED; // Any recovery should be done by spawning a new command

        case REQUESTING_REMOTE_CHANGE :
            return requestRemoteChange() ?
                    SUCCESSFUL : REQUEST_FAILED;

        case REQUEST_FAILED:
            handleRequestFailed();
            return CANCELLED; // Any recovery should be done by spawning a new command

        default :
            throw new AssertionError("Incomplete implementation!"); // Implement handling for any new states!
        }
    }

    /**
     * Retrieves and stores command input via side effects.
     * @see #simulateResult()
     * @return true if valid input was received, false if invalid or cancelled.
     */
    protected abstract boolean retrieveValidInput();

    /**
     * Optimistically simulates the result of this command.
     * @see #retrieveValidInput()
     */
    protected abstract void simulateResult();

    /**
     * State transition for {@link State#GRACE_PERIOD}.
     * This grace period phase allows the user to cancel the command with minimal cost.
     *
     * Updates {@link #handleChangeToSecondsLeftInGracePeriod(int)} whenever seconds remaining in the
     * grace period countdown changes.
     *
     * @see #cancelCommand()
     * @return next state
     */
    private State gracePeriodCountdownAndTransition() {
        // Countdown loop, checks for cancel signal
        for (int i = gracePeriodDurationInSeconds; i > 0; i--) {
            handleChangeToSecondsLeftInGracePeriod(i);

            try {
                // wait 1 second for cancellation signal
                if (waitForCancelRequest(1, TimeUnit.SECONDS)) {
                    handleChangeToSecondsLeftInGracePeriod(0); // signify end of grace period
                    return CANCELLED;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        handleChangeToSecondsLeftInGracePeriod(0); // signify end of grace period
        return CHECKING_REMOTE_CONFLICT; // not cancelled
    }

    /**
     * Hook that gets called every time number of seconds left in the grace period changes.
     * @see #gracePeriodCountdownAndTransition()
     */
    protected abstract void handleChangeToSecondsLeftInGracePeriod(int secondsLeft);

    /**
     * Checks for any unseen remote changes to this command's target since this command was started.
     * @see State#CONFLICT_FOUND
     * @return true if a remote conflict was found, false otherwise.
     */
    protected abstract boolean checkForRemoteConflict();

    /**
     * Runs when {@link State#CONFLICT_FOUND} is reached.
     * After this method completes, the command is {@link State#CANCELLED}, perform all prompts and informing the user
     * in this method, then recovery should utilise a new command.
     * @see #handleRequestFailed()
     */
    protected abstract void handleRemoteConflict();

    /**
     * State transition for {@link State#REQUESTING_REMOTE_CHANGE}
     *
     * Sends a request for the change encapsulated by this command to be performed on the remote.
     * Handles the response from the remote server.
     *
     * @see State#SUCCESSFUL
     * @see State#REQUEST_FAILED
     * @return true if the change was successful, false otherwise
     */
    protected abstract boolean requestRemoteChange();

    /**
     * Runs when {@link State#REQUEST_FAILED} is reached.
     * After this method completes, the command is {@link State#CANCELLED}, perform all prompts and informing the user
     * in this method, then recovery should utilise a new command.
     * @see #handleRemoteConflict()
     */
    protected abstract void handleRequestFailed();

}
