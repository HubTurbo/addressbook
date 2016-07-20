package address.model;

import address.model.datatypes.person.ReadOnlyPerson;
import commons.PlatformExecUtil;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Base class for individual-person level changes like add, edit, delete.
 */
public abstract class ChangePersonInModelCommand extends ChangeObjectInModelCommand {

    public static final String TARGET_TYPE = "Person";

    protected Supplier<Optional<ReadOnlyPerson>> inputRetriever;
    protected ReadOnlyPerson input;

    Optional<ReadOnlyPerson> remoteConflictData;
    CompletableFuture<Runnable> recoveryCallback;

    {
        remoteConflictData = Optional.empty();
    }

    /**
     * @param inputRetriever Will run on execution {@link #run()} thread. This should handle thread concurrency
     *                       logic (eg. {@link PlatformExecUtil#call(Callable)} within itself. If the returned Optional
     *                       is empty, it means input could not be retrieved and the command will be cancelled.
     */
    protected ChangePersonInModelCommand(int commandId, Supplier<Optional<ReadOnlyPerson>> inputRetriever,
                                         int gracePeriodDurationInSeconds) {
        super(commandId, gracePeriodDurationInSeconds);
        this.inputRetriever = inputRetriever;
    }

    /**
     * @return ID of the target person of this command
     */
    public abstract int getTargetPersonId();

    /**
     * Request to override this command with an edit command.
     * @param editInputSupplier supplies input for the overriding edit command
     */
    public void overrideWithEditPerson(Supplier<Optional<ReadOnlyPerson>> editInputSupplier) {
        assert !getState().isTerminal() : "Attempted to override a terminated command";
        pauseGracePeriod();
        handleEditRequest(editInputSupplier);
        resumeGracePeriod();
    }

    /**
     * Command-specific handling logic of an overriding edit request
     * @see #overrideWithEditPerson(Supplier)
     */
    protected abstract void handleEditRequest(Supplier<Optional<ReadOnlyPerson>> editInputSupplier);

    /**
     * Request to override this command with a delete command.
     * @see #overrideWithEditPerson(Supplier)
     */
    public void overrideWithDeletePerson() {
        assert !getState().isTerminal() : "Attempted to override a terminated command";
        pauseGracePeriod();
        handleDeleteRequest();
        resumeGracePeriod();
    }

    /**
     * Command-specific handling logic of an overriding delete request
     * @see #overrideWithDeletePerson()
     */
    protected abstract void handleDeleteRequest();

    /**
     * Resolve the remote conflict.
     */
    public void resolveConflict() {
        assert getState() != CommandState.CONFLICT_FOUND : "Attempted to resolve conflict for a command without a detected conflict";
        handleResolveConflict();
    }

    /**
     * Command specific handler logic for resolving a remote conflict
     * @see #resolveConflict()
     */
    protected abstract void handleResolveConflict();

    /**
     * Request to retry the same command.
     */
    public void retry() {
        assert getState() != CommandState.REQUEST_FAILED : "Attempted to retry a command that has not failed";
        handleRetry();
    }

    /**
     * Command specific handling of retry
     * @see #retry()
     */
    protected abstract void handleRetry();

    @Override
    protected boolean retrieveValidInput() {
        final Optional<ReadOnlyPerson> retrieved = inputRetriever.get();
        if (retrieved.isPresent()) {
            this.input = retrieved.get();
            logger.debug("retrieveInput: Retrieving input " + retrieved.get().toString());
        }
        // Not present = problem retrieving input (most likely user cancelled input dialog or some exception occurred.
        return retrieved.isPresent();
    }

    @Override
    protected boolean checkForRemoteConflict() {
        final Optional<ReadOnlyPerson> conflict = getRemoteConflictData();
        return conflict.isPresent();
    }

    /**
     * Retrieves the remote's version of the Person in question if there was a conflicting change on the server.
     * @return contains the remote's version, else empty if no conflict
     */
    protected abstract Optional<ReadOnlyPerson> getRemoteConflictData();

    /**
     * Possible actions in conflict stage:
     * @see #cancelCommand()
     * @see #resolveConflict()
     */
    @Override
    protected void handleRemoteConflict() {
        whenRemoteConflictDetected();
        try {
            waitForCancelRequest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected abstract void whenRemoteConflictDetected();

    /**
     * Possible actions in request failed stage:
     * @see #cancelCommand()
     * @see #retry()
     */
    @Override
    protected void handleRequestFailed() {
        whenRemoteRequestFailed();
        try {
            waitForCancelRequest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected abstract void whenRemoteRequestFailed();

}
