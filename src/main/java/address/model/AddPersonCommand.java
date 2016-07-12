package address.model;

import static address.model.ChangeObjectInModelCommand.State.*;
import static address.model.datatypes.person.ReadOnlyViewablePerson.ChangeInProgress.*;

import address.events.BaseEvent;
import address.events.CreatePersonOnRemoteRequestEvent;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.person.ViewablePerson;
import address.util.AppLogger;
import address.util.LoggerManager;
import address.util.PlatformExecUtil;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Handles optimistic UI updating, cancellation/changing command, and remote consistency logic
 * for adding a person to the addressbook.
 */
public class AddPersonCommand extends ChangePersonInModelCommand {

    public static final String COMMAND_TYPE = "Add Person";

    private final Consumer<BaseEvent> eventRaiser;
    private final ModelManager model;
    private ViewablePerson viewableToAdd;
    private final String addressbookName;

    // Data snapshot for building result object
    private ReadOnlyPerson personDataSnapshot = null; // null if command terminated before input composed

    /**
     * @param inputRetriever Will run on execution {@link #run()} thread. This should handle thread concurrency
     *                       logic (eg. {@link PlatformExecUtil#call(Callable)} within itself.
     *                       If the returned Optional is empty, the command will be cancelled.
     * @see super#ChangePersonInModelCommand(int, Supplier, int)
     */
    public AddPersonCommand(int commandId, Supplier<Optional<ReadOnlyPerson>> inputRetriever, int gracePeriodDurationInSeconds,
                               Consumer<BaseEvent> eventRaiser, ModelManager model, String addressbookName) {
        super(commandId, inputRetriever, gracePeriodDurationInSeconds);
        this.model = model;
        this.eventRaiser = eventRaiser;
        this.addressbookName = addressbookName;
    }

    protected ViewablePerson getViewableToAdd() {
        return viewableToAdd;
    }

    @Override
    public int getTargetPersonId() {
        if (viewableToAdd == null) {
            throw new IllegalStateException("Add person command has not created the proposed ViewablePerson yet.");
        }
        return viewableToAdd.getId();
    }

    @Override
    protected void before() {
        // N/A
    }

    @Override
    protected void after() {
        if (viewableToAdd != null) {
            viewableToAdd.setChangeInProgress(NONE);
            model.unassignOngoingChangeForPerson(viewableToAdd.getId());
        }
        // personDataSnapshot == null means that the command was cancelled before any input was received
        final String targetName = personDataSnapshot == null ? "" : personDataSnapshot.fullName();
        final String targetIdString = personDataSnapshot == null ? "" : personDataSnapshot.idString();
        model.trackCommandResult(new SingleTargetCommandResult(getCommandId(), COMMAND_TYPE, getState().toResultStatus(),
                                TARGET_TYPE, targetIdString, targetName, targetName));
    }

    /**
     * Creates the viewableperson and adds it to the visible person list in model.
     * It will have no backing person at first (backing person is connected only after remote confirmation)
     */
    @Override
    protected State simulateResult() {
        assert input != null;
        // create VP and add to model
        PlatformExecUtil.runAndWait(() -> {
            viewableToAdd = model.addViewablePersonWithoutBacking(input);
            viewableToAdd.setChangeInProgress(ADDING);
        });
        snapshotPersonData(viewableToAdd);
        model.assignOngoingChangeToPerson(viewableToAdd.getId(), this);
        return GRACE_PERIOD;
    }

    @Override
    protected void handleChangeToSecondsLeftInGracePeriod(int secondsLeft) {
        assert viewableToAdd != null;
        PlatformExecUtil.runAndWait(() -> viewableToAdd.setSecondsLeftInPendingState(secondsLeft));
    }

    @Override
    protected State handleEditInGracePeriod(Supplier<Optional<ReadOnlyPerson>> editInputSupplier) {
        // take details and update viewable, then restart grace period
        final Optional<ReadOnlyPerson> editInput = editInputSupplier.get();
        if (editInput.isPresent()) { // edit request confirmed
            input = editInput.get(); // update saved input
            PlatformExecUtil.runAndWait(() -> viewableToAdd.simulateUpdate(input));
            snapshotPersonData(viewableToAdd);
        }
        return GRACE_PERIOD; // restart grace period
    }

    @Override
    protected State handleDeleteInGracePeriod() {
        // undo the addition, no need to inform remote because nothing happened from their point of view.
        return CANCELLED;
    }

    @Override
    protected Optional<ReadOnlyPerson> getRemoteConflict() {
        return Optional.empty(); // no possible conflict for add command
    }

    @Override
    protected State resolveRemoteConflict(ReadOnlyPerson remoteVersion) {
        assert false; // no possible conflict for add command
        return REQUESTING_REMOTE_CHANGE;
    }

    @Override
    protected State requestChangeToRemote() {
        assert input != null;

        CompletableFuture<ReadOnlyPerson> responseHolder = new CompletableFuture<>();
        eventRaiser.accept(new CreatePersonOnRemoteRequestEvent(responseHolder, addressbookName, input));
        try {
            final Person backingPerson = new Person(responseHolder.get());

            model.unassignOngoingChangeForPerson(getTargetPersonId()); // removes mapping for old id

            PlatformExecUtil.runAndWait(() -> {
                model.addPersonToBackingModelSilently(backingPerson); // so it wont trigger creation of another VP
                viewableToAdd.connectBackingObject(backingPerson); // changes id to that of backing person
                model.assignOngoingChangeToPerson(backingPerson.getId(), this); // remap this change for the new id
            });
            snapshotPersonData(viewableToAdd);
            return SUCCESSFUL;
        } catch (ExecutionException | InterruptedException e) {
            return FAILED;
        }
    }

    @Override
    protected void finishWithCancel() {
        if (viewableToAdd != null) {
            PlatformExecUtil.runAndWait(() -> model.visibleModel().removePerson(viewableToAdd.getId()));
        }
    }

    @Override
    protected void finishWithSuccess() {
        // maybe some logging
    }

    @Override
    protected void finishWithFailure() {
        finishWithCancel(); // TODO figure out failure handling
    }

    private void snapshotPersonData(ReadOnlyPerson data) {
        personDataSnapshot = new Person(data);
    }

}
