package address.model;

import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.person.ViewablePerson;
import address.util.PlatformExecUtil;

import static address.model.ChangeObjectInModelCommand.State.*;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Base class for individual-person level changes like add, edit, delete.
 * Adds some logic to {@link #checkAndHandleRemoteConflict()}.
 * Also extends {@link ChangeObjectInModelCommand} with 'edit' and 'delete' grace period overriding signalling methods:
 * {@link #editInGracePeriod(Supplier)}, {@link #deleteInGracePeriod()}.
 */
public abstract class ChangePersonInModelCommand extends ChangeObjectInModelCommand {

    protected Supplier<Optional<ReadOnlyPerson>> inputRetriever;
    protected ReadOnlyPerson input;

    /**
     *
     * @param inputRetriever Will run on execution {@link #run()} thread. This should handle external thread execution
     *                       logic (eg. {@link PlatformExecUtil#callLater(Callable)} within itself.
     *                       If the returned Optional is empty, the command will be cancelled.
     */
    protected ChangePersonInModelCommand(Supplier<Optional<ReadOnlyPerson>> inputRetriever,
                                         int gracePeriodDurationInSeconds) {
        super(gracePeriodDurationInSeconds);
    }

    @Override
    protected State retrieveInput() {
        final Optional<ReadOnlyPerson> input = inputRetriever.get();
        if (input.isPresent()) {
            this.input = input.get();
            return SIMULATING_RESULT; // normal exec path
        }
        // Problem retrieving input (most likely user cancelled input dialog or some exception occurred.
        return CANCELLED;
    }

    /**
     * Request to override this command with an edit command using {@code newInputSupplier} to supply input.
     * Only works if the command is currently in the {@link State#GRACE_PERIOD} state.
     *
     * @see super#signalGracePeriodOverride(Supplier)
     * @param newInputSupplier supplies input for the overriding edit command
     */
    synchronized void editInGracePeriod(Supplier<Optional<ReadOnlyPerson>> newInputSupplier) {
        signalGracePeriodOverride(() -> handleEditInGracePeriod(newInputSupplier));
    }

    /**
     * Request to override this command with a delete command.
     * Only works if the command is currently in the {@link State#GRACE_PERIOD} state.
     *
     * @see super#signalGracePeriodOverride(Supplier)
     * @see #editInGracePeriod(Supplier)
     */
    synchronized void deleteInGracePeriod() {
        signalGracePeriodOverride(this::handleDeleteInGracePeriod);
    }

    /**
     * When the 'edit' overriding signal is received (from {@link #editInGracePeriod(Supplier)}) during the grace period,
     * this method takes over the state transition path and execution.
     * Will be called from the {@link #run()} thread.
     *
     * @see #editInGracePeriod(Supplier)
     * @see super#handleCancelInGracePeriod()
     * @param editInputSupplier supplies data for the edit
     * @return next state (current state will be {@link State#GRACE_PERIOD})
     */
    protected abstract State handleEditInGracePeriod(Supplier<Optional<ReadOnlyPerson>> editInputSupplier);

    /**
     * When the 'delete' overriding signal is received (from {@link #deleteInGracePeriod()}) during the grace period,
     * this method takes over the state transition path and execution.
     * Will be called from the {@link #run()} thread.
     *
     * @see #deleteInGracePeriod()
     * @see super#handleCancelInGracePeriod()
     * @return next state (current state will be {@link State#GRACE_PERIOD})
     */
    protected abstract State handleDeleteInGracePeriod();

    @Override
    protected State checkAndHandleRemoteConflict() {
        final Optional<ReadOnlyPerson> conflict = getRemoteConflict();
        if (conflict.isPresent()) {
            return resolveRemoteConflict(conflict.get());
        } else {
            return REQUESTING_REMOTE_CHANGE;
        }
    }

    /**
     * Retrieves the remote's version of the Person in question if there was a conflicting change on the server.
     * @return contains the remote's version, else empty if no conflict
     */
    protected abstract Optional<ReadOnlyPerson> getRemoteConflict();

    /**
     * Logic to handle the remote conflict.
     * @param remoteVersion version of the Person on the remote server
     * @return next state (current state will be {@link State#CHECKING_AND_RESOLVING_REMOTE_CONFLICT}
     */
    protected abstract State resolveRemoteConflict(ReadOnlyPerson remoteVersion);
}
