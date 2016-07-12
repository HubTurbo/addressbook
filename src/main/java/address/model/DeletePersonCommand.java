package address.model;

import static address.model.ChangeObjectInModelCommand.State.*;
import static address.model.datatypes.person.ReadOnlyViewablePerson.ChangeInProgress.*;

import address.events.BaseEvent;
import address.events.DeletePersonOnRemoteRequestEvent;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.person.ViewablePerson;
import address.util.PlatformExecUtil;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Handles optimistic UI updating, cancellation/changing command, and remote consistency logic
 * for deleting a person from the addressbook.
 */
public class DeletePersonCommand extends ChangePersonInModelCommand {

    public static final String COMMAND_TYPE = "Delete Person";

    private final Consumer<BaseEvent> eventRaiser;
    private final ModelManager model;
    private final ViewablePerson target;
    private final String addressbookName;

    // Person state snapshots
    private ReadOnlyPerson personDataBeforeExecution;

    /**
     * @see super#ChangePersonInModelCommand(int, Supplier, int)
     */
    public DeletePersonCommand(int commandId, ViewablePerson target, int gracePeriodDurationInSeconds,
                               Consumer<BaseEvent> eventRaiser, ModelManager model, String addressbookName) {
        // no input needed for delete commands
        super(commandId, () -> Optional.of(target), gracePeriodDurationInSeconds);
        this.target = target;
        this.model = model;
        this.eventRaiser = eventRaiser;
        this.addressbookName = addressbookName;
    }

    protected ViewablePerson getViewable() {
        return target;
    }

    @Override
    public int getTargetPersonId() {
        return target.getId();
    }

    @Override
    protected void before() {
        personDataBeforeExecution = new Person(target);
        if (model.personHasOngoingChange(target)) {
            try {
                model.getOngoingChangeForPerson(target).waitForCompletion();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        PlatformExecUtil.runAndWait(() -> target.setChangeInProgress(DELETING));
        model.assignOngoingChangeToPerson(target.getId(), this);
        target.stopSyncingWithBackingObject();
    }

    @Override
    protected void after() {
        PlatformExecUtil.runAndWait(() -> {
            target.setChangeInProgress(NONE);
            target.continueSyncingWithBackingObject();
            target.forceSyncFromBacking();
        });
        model.unassignOngoingChangeForPerson(target.getId());
        final String targetName = personDataBeforeExecution.fullName(); // no name changes for deletes
        model.trackCommandResult(new SingleTargetCommandResult(getCommandId(), COMMAND_TYPE, getState().toResultStatus(),
                                TARGET_TYPE, target.idString(), targetName, targetName));
    }

    @Override
    protected State simulateResult() {
        // delete changeinprogress field already set in #before
        return GRACE_PERIOD;
    }

    @Override
    protected void handleChangeToSecondsLeftInGracePeriod(int secondsLeft) {
        PlatformExecUtil.runAndWait(() -> target.setSecondsLeftInPendingState(secondsLeft));
    }

    @Override
    protected State handleEditInGracePeriod(Supplier<Optional<ReadOnlyPerson>> editInputSupplier) {
        model.execNewEditPersonCommand(target, editInputSupplier);
        return CANCELLED;
    }

    @Override
    protected State handleDeleteInGracePeriod() {
        return GRACE_PERIOD; // nothing to be done
    }

    @Override
    protected Optional<ReadOnlyPerson> getRemoteConflict() {
        return Optional.empty(); // TODO add after cloud individual check implemented
    }

    @Override
    protected State resolveRemoteConflict(ReadOnlyPerson remoteVersion) {
        assert false; // TODO figure out what to show to users
        return null;
    }

    @Override
    protected State requestChangeToRemote() {
        // TODO: update when remote request api is complete
        CompletableFuture<Boolean> responseHolder = new CompletableFuture<>();
        eventRaiser.accept(new DeletePersonOnRemoteRequestEvent(responseHolder, addressbookName, target.getId()));
        try {
            responseHolder.get();
            PlatformExecUtil.runAndWait(() -> model.backingModel().removePerson(target));
            return SUCCESSFUL;
        } catch (ExecutionException | InterruptedException e) {
            return FAILED;
        }
    }

    @Override
    protected void finishWithCancel() {
        // nothing needed
    }

    @Override
    protected void finishWithSuccess() {
        // Nothing to do for now
    }

    @Override
    protected void finishWithFailure() {
        finishWithCancel(); // TODO figure out failure handling
    }
}
