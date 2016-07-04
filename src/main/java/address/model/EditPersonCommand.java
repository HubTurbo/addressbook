package address.model;

import static address.model.ChangeObjectInModelCommand.State.*;
import address.events.BaseEvent;
import address.events.LocalModelChangedEvent;
import address.events.UpdatePersonOnRemoteRequestEvent;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.person.ViewablePerson;
import address.util.PlatformExecUtil;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Handles optimistic UI updating, cancellation/changing command, and remote consistency logic
 * for editing a person in the addressbook.
 */
public class EditPersonCommand extends ChangePersonInModelCommand {

    private final Consumer<BaseEvent> eventRaiser;
    private final ModelManager model;
    private final ViewablePerson target;
    private final String addressbookName;

    /**
     * @param inputRetriever               Will run on execution {@link #run()} thread. This should handle thread concurrency
     *                                     logic (eg. {@link PlatformExecUtil#call(Callable)} within itself.
     *                                     If the returned Optional is empty, the command will be cancelled.
     * @see super#ChangePersonInModelCommand(Supplier, int)
     */
    public EditPersonCommand(ViewablePerson target, Supplier<Optional<ReadOnlyPerson>> inputRetriever,
                                int gracePeriodDurationInSeconds, Consumer<BaseEvent> eventRaiser,
                                ModelManager model) {
        super(inputRetriever, gracePeriodDurationInSeconds);
        assert target != null;
        this.target = target;
        this.model = model;
        this.eventRaiser = eventRaiser;
        this.addressbookName = model.getPrefs().getSaveFileName();
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
            target.continueSyncingWithBackingObject();
            target.forceSyncFromBacking();
            target.setIsEdited(false);
        });
        model.unassignOngoingChangeForPerson(target.getId());
    }

    @Override
    protected State simulateResult() {
        assert input != null;
        PlatformExecUtil.runAndWait(() -> {
            target.setIsEdited(true);
            target.simulateUpdate(input);
        });
        return GRACE_PERIOD;
    }

    @Override
    protected void handleChangeToSecondsLeftInGracePeriod(int secondsLeft) {
        PlatformExecUtil.runAndWait(() -> target.setSecondsLeftInPendingState(secondsLeft));
    }

    @Override
    protected State handleEditInGracePeriod(Supplier<Optional<ReadOnlyPerson>> editInputSupplier) {
        // take details and update viewable, then restart grace period
        final Optional<ReadOnlyPerson> editInput = editInputSupplier.get();
        if (editInput.isPresent()) { // edit request confirmed
            input = editInput.get(); // update saved input
            PlatformExecUtil.runAndWait(() -> target.simulateUpdate(input));
        }
        return GRACE_PERIOD; // restart grace period
    }

    @Override
    protected State handleDeleteInGracePeriod() {
        model.execNewDeletePersonCommand(target);
        return CANCELLED;
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
        assert input != null;

        CompletableFuture<ReadOnlyPerson> responseHolder = new CompletableFuture<>();
        eventRaiser.accept(new UpdatePersonOnRemoteRequestEvent(responseHolder, addressbookName, target.getId(), input));
        try {
            responseHolder.get();
            PlatformExecUtil.runAndWait(() -> target.getBacking().update(input));
            return SUCCESSFUL;
        } catch (ExecutionException | InterruptedException e) {
            return CANCELLED; // figure out a policy for syncup fail
        }
    }

    @Override
    protected void finishWithCancel() {
        // for now, already handled by #after()
    }

    @Override
    protected void finishWithSuccess() {
        model.raiseLocalModelChangedEvent();
    }

    @Override
    protected void finishWithFailure() {
        // can't happen yet
    }
}
