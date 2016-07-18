package address.model;

import static address.model.ChangeObjectInModelCommand.State.*;
import static address.model.datatypes.person.ReadOnlyViewablePerson.ChangeInProgress.*;

import address.events.BaseEvent;
import address.events.CommandFinishedEvent;
import address.events.CreatePersonOnRemoteRequestEvent;
import address.model.datatypes.person.Person;
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
 * for adding a person to the addressbook.
 */
public class AddPersonCommand extends ChangePersonInModelCommand {

    public static final String COMMAND_TYPE = "Add";

    private final Consumer<BaseEvent> eventRaiser;
    private final ModelManager model;
    private ViewablePerson viewableToAdd;
    private Person backingFromRemote;
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
        if (viewableToAdd != null) { // the viewable was already added
            viewableToAdd.clearChangeInProgress();
            model.unassignOngoingChangeForPerson(viewableToAdd.getId());
        }
        // personDataSnapshot == null means that the command was cancelled before any input was received
        final String targetName = personDataSnapshot == null ? "" : personDataSnapshot.fullName();
        final String targetIdString = personDataSnapshot == null ? "" : personDataSnapshot.idString();
        eventRaiser.accept(new CommandFinishedEvent(
                new SingleTargetCommandResult(getCommandId(), COMMAND_TYPE, getState().toResultStatus(), TARGET_TYPE,
                        targetIdString, targetName, targetName)
        ));
    }

    /**
     * Creates the viewableperson and adds it to the visible person list in model.
     * It will have no backing person at first (backing person is connected only after remote confirmation)
     */
    @Override
    protected void simulateResult() {
        assert input != null;
        // create VP and add to model
        PlatformExecUtil.runAndWait(() -> {
            viewableToAdd = model.addViewablePersonWithoutBacking(input);
            viewableToAdd.setChangeInProgress(ADDING);
        });

        model.assignOngoingChangeToPerson(viewableToAdd.getId(), this);
        snapshotPersonData(viewableToAdd);
    }

    @Override
    protected void handleChangeToSecondsLeftInGracePeriod(int secondsLeft) {
        assert viewableToAdd != null;
        PlatformExecUtil.runAndWait(() -> viewableToAdd.setSecondsLeftInPendingState(secondsLeft));
    }

    @Override
    protected void handleEditRequest(Supplier<Optional<ReadOnlyPerson>> editInputSupplier) {
        final Optional<ReadOnlyPerson> editInput = editInputSupplier.get();
        if (editInput.isPresent()) { // edit request confirmed
            model.execNewAddPersonCommand(() -> editInput); // spawn new add request with the updated info
        }
    }

    @Override
    protected void handleDeleteRequest() {
        // do nothing, let it get cancelled
    }

    @Override
    protected void handleResolveConflict() {
        // No conflicts possibl
    }

    @Override
    protected void handleRetry() {
        model.execNewAddPersonCommand(() -> Optional.of(input));
    }

    @Override
    protected Optional<ReadOnlyPerson> getRemoteConflictData() {
        return Optional.empty(); // no possible conflict for add command
    }

    @Override
    protected boolean requestRemoteChange() {
        assert input != null;

        CompletableFuture<ReadOnlyPerson> responseHolder = new CompletableFuture<>();
        eventRaiser.accept(new CreatePersonOnRemoteRequestEvent(responseHolder, addressbookName, input));
        try {
            backingFromRemote = new Person(responseHolder.get());
            return true;
        } catch (ExecutionException | InterruptedException e) {
            return false;
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
        PlatformExecUtil.runAndWait(() -> {
            model.addPersonToBackingModelSilently(backingFromRemote); // so it wont trigger creation of another VP
            model.unassignOngoingChangeForPerson(getTargetPersonId()); // removes mapping for old id
            viewableToAdd.connectBackingObject(backingFromRemote); // changes id to that of backing person
            model.assignOngoingChangeToPerson(viewableToAdd.getId(), this); // remap this change for the new id
        });
        snapshotPersonData(viewableToAdd); // update snapshot for remote assigned id
    }

    private void snapshotPersonData(ReadOnlyPerson data) {
        personDataSnapshot = new Person(data);
    }

}
