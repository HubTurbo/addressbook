package address.model;

import static address.model.datatypes.person.ReadOnlyViewablePerson.*;

import address.events.BaseEvent;
import address.events.sync.DeletePersonOnRemoteRequestEvent;
import address.events.model.SingleTargetCommandResultEvent;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.person.ViewablePerson;
import commons.PlatformExecUtil;

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

    public static final String COMMAND_TYPE = "Delete";

    private final Consumer<BaseEvent> eventRaiser;
    private final ModelManager model;
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
        model.assignOngoingChangeToPerson(target.getId(), this);
        target.stopSyncingWithBackingObject();
    }

    @Override
    protected void after() {
        PlatformExecUtil.runAndWait(() -> {
            target.clearOngoingCommand();
            target.continueSyncingWithBackingObject();
            target.forceSyncFromBacking();
        });
        model.unassignOngoingChangeForPerson(target.getId());

        // catches CANCELLED and SUCCESS
        eventRaiser.accept(new SingleTargetCommandResultEvent(getCommandId(), COMMAND_TYPE, getState(),
                TARGET_TYPE, target.idString(), target.fullName(), target.fullName()));
    }

    @Override
    protected void simulateResult() {
        PlatformExecUtil.runAndWait(() -> target.setOngoingCommandType(OngoingCommandType.DELETING));
    }

    @Override
    protected void handleEditRequest(Supplier<Optional<ReadOnlyPerson>> editInputSupplier) {
        cancelCommand();
        model.execNewEditPersonCommand(target, editInputSupplier);
    }

    @Override
    protected void handleDeleteRequest() {
        // nothing to do here
    }

    @Override
    protected void handleResolveConflict() {
        // TODO
    }

    @Override
    protected void handleRetry() {
        cancelCommand();
        model.execNewDeletePersonCommand(target);
    }

    @Override
    protected Optional<ReadOnlyPerson> getRemoteConflictData() {
        return Optional.empty(); // TODO add after cloud individual check implemented
    }

    @Override
    protected void handleRemoteConflict() {
        eventRaiser.accept(new SingleTargetCommandResultEvent(getCommandId(), COMMAND_TYPE, getState(),
                TARGET_TYPE, target.idString(), target.fullName(), target.fullName()));
        super.handleRemoteConflict();
    }

    @Override
    protected void handleRequestFailed() {
        eventRaiser.accept(new SingleTargetCommandResultEvent(getCommandId(), COMMAND_TYPE, getState(),
                TARGET_TYPE, target.idString(), target.fullName(), target.fullName()));
        super.handleRequestFailed();
    }

    @Override
    protected boolean requestRemoteChange() {
        final CompletableFuture<Boolean> responseHolder = new CompletableFuture<>();
        eventRaiser.accept(new DeletePersonOnRemoteRequestEvent(responseHolder, addressbookName, target.getId()));
        try {
            return responseHolder.get();
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }
    }

    @Override
    protected void finishWithCancel() {
        // nothing needed
    }

    @Override
    protected void finishWithSuccess() {
        // removing from backing will remove the front facing viewable too
        PlatformExecUtil.runAndWait(() -> model.backingModel().removePerson(target));
    }

}
