package address.model;

import address.util.AppLogger;
import address.util.LoggerManager;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

import java.util.concurrent.*;

import static address.model.ChangeObjectInModelCommand.CommandState.*;

/**
 * Framework-style superclass for all commands that would cause changes for single domain objects in the model,
 * and have it optimistically reflected on the UI. Internal logic approximates a finite state machine.
 *
 * Should be run OUTSIDE THE FX THREAD because the {@link #run()} method involves blocking calls.
 */
public abstract class ChangeObjectInModelCommand implements Runnable {

    public enum CommandState {
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
        CANCELLED                   ("Cancelled"),
        SUCCESSFUL                  ("Successful");

        private final String descr;
        CommandState(String descr) {
            this.descr = descr;
        }
        @Override
        public String toString() {
            return descr;
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
    protected final Property<CommandState> state; // current state

    private final CountDownLatch completionLatch; // blocking completion flag
    private final CountDownLatch cancelledLatch; // blocking cancellation flag
    private CountDownLatch pauseGracePeriodLatch;
    private CountDownLatch resumeGracePeriodLatch;
    
    {
        completionLatch = new CountDownLatch(1); // irreversible flag
        cancelledLatch = new CountDownLatch(1); // irreversible flag
        pauseGracePeriodLatch = new CountDownLatch(1);
        resumeGracePeriodLatch = new CountDownLatch(1);
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
        pauseGracePeriod();
        cancelledLatch.countDown();
        resumeGracePeriod();
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

    protected boolean isCancelRequested() {
        return cancelledLatch.getCount() == 0;
    }

    public CommandState getState() {
        return state.getValue();
    }

    void setState(CommandState newState) {
        state.setValue(newState);
    }

    /**
     * Hook that runs before new states are handled
     */
    protected void beforeState(CommandState currentState) {
        // override to inject code
    }

    /**
     * Hook that runs after a state is handled
     */
    protected void afterState(CommandState handledState) {
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
     * Runs when the terminal {@link CommandState#CANCELLED} state is reached.
     */
    protected abstract void finishWithCancel();

    /**
     * Runs when the terminal {@link CommandState#SUCCESSFUL} state is reached.
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
        handleTerminalState();

        after();
        completionLatch.countDown();
    }

    private void handleTerminalState() {
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
        afterState(getState());
    }

    /**
     * FSM engine to multiplex and run side-effect logic transition state
     * @param state state to be considered
     * @return next state
     */
    private CommandState handleAndTransitionState(CommandState state) {
        beforeState(state);
        final CommandState next;
        switch (state) {
        case NEWLY_CREATED :
            next = RETRIEVING_INPUT;
            break;

        case RETRIEVING_INPUT :
            next = retrieveValidInput() ? SIMULATING_RESULT : CANCELLED;
            break;

        case SIMULATING_RESULT :
            simulateResult();
            next = GRACE_PERIOD;
            break;

        case GRACE_PERIOD:
            next = gracePeriodCountdownAndTransition();
            break;

        case CHECKING_REMOTE_CONFLICT:
            next = checkForRemoteConflict() ? CONFLICT_FOUND : REQUESTING_REMOTE_CHANGE;
            break;

        case CONFLICT_FOUND:
            handleRemoteConflict();
            next = CANCELLED; // Any recovery should be done by spawning a new command
            break;

        case REQUESTING_REMOTE_CHANGE :
            next = requestRemoteChange() ? SUCCESSFUL : REQUEST_FAILED;
            break;

        case REQUEST_FAILED:
            handleRequestFailed();
            next = CANCELLED; // Any recovery should be done by spawning a new command
            break;

        default :
            throw new AssertionError("Incomplete implementation!"); // Implement handling for any new states!
        }
        afterState(state);
        return next;
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
     * State transition for {@link CommandState#GRACE_PERIOD}.
     * This grace period phase allows the user to cancel the command with minimal cost.
     *
     * Updates {@link #handleChangeToSecondsLeftInGracePeriod(int)} whenever seconds remaining in the
     * grace period countdown changes.
     *
     * @see #cancelCommand()
     * @return next state
     */
    protected CommandState gracePeriodCountdownAndTransition() {
        // Countdown loop, checks for cancel signal
        for (int i = gracePeriodDurationInSeconds; i > 0; i--) {
            handleChangeToSecondsLeftInGracePeriod(i);
            try {
                // wait 1 second each time for interruptions
                if (pauseGracePeriodLatch.await(1, TimeUnit.SECONDS)) {
                    pauseGracePeriodLatch = new CountDownLatch(1); // reset for future pauses
                    resumeGracePeriodLatch.await(); // wait to resume execution
                    resumeGracePeriodLatch = new CountDownLatch(1); // reset for future pauses
                    if (isCancelRequested()) {
                        handleChangeToSecondsLeftInGracePeriod(0); // end grace period
                        return CANCELLED;
                    }
                    i = gracePeriodDurationInSeconds; // unpaused but not cancelled, reset countdown
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        handleChangeToSecondsLeftInGracePeriod(0); // signify end of grace period
        return CHECKING_REMOTE_CONFLICT; // not cancelled
    }

    protected void pauseGracePeriod() {
        pauseGracePeriodLatch.countDown();
    }

    protected void resumeGracePeriod() {
        resumeGracePeriodLatch.countDown();
    }

    /**
     * Hook that gets called every time number of seconds left in the grace period changes.
     * @see #gracePeriodCountdownAndTransition()
     */
    protected abstract void handleChangeToSecondsLeftInGracePeriod(int secondsLeft);

    /**
     * Checks for any unseen remote changes to this command's target since this command was started.
     * @see CommandState#CONFLICT_FOUND
     * @return true if a remote conflict was found, false otherwise.
     */
    protected abstract boolean checkForRemoteConflict();

    /**
     * Runs when {@link CommandState#CONFLICT_FOUND} is reached.
     * After this method completes, the command is {@link CommandState#CANCELLED}, perform all prompts and informing the user
     * in this method, then recovery should utilise a new command.
     * @see #handleRequestFailed()
     */
    protected abstract void handleRemoteConflict();

    /**
     * State transition for {@link CommandState#REQUESTING_REMOTE_CHANGE}
     *
     * Sends a request for the change encapsulated by this command to be performed on the remote.
     * Handles the response from the remote server.
     *
     * @see CommandState#SUCCESSFUL
     * @see CommandState#REQUEST_FAILED
     * @return true if the change was successful, false otherwise
     */
    protected abstract boolean requestRemoteChange();

    /**
     * Runs when {@link CommandState#REQUEST_FAILED} is reached.
     * After this method completes, the command is {@link CommandState#CANCELLED}, perform all prompts and informing the user
     * in this method, then recovery should utilise a new command.
     * @see #handleRemoteConflict()
     */
    protected abstract void handleRequestFailed();

}
