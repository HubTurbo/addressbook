package address.model;

import static address.model.datatypes.person.ReadOnlyViewablePerson.*;

import address.events.BaseEvent;
import address.events.CreatePersonOnRemoteRequestEvent;
import address.events.SingleTargetCommandResultEvent;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.model.datatypes.person.ViewablePerson;
import commons.PlatformExecUtil;

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

    @Override
    public int getTargetPersonId() {
        if (target == null) {
            throw new IllegalStateException("Add person command has not created the proposed ViewablePerson yet.");
        }
        return target.getId();
    }

    @Override
    protected void before() {
        // N/A
    }

    @Override
    protected void after() {
        if (target != null) { // the viewable was already added
            target.clearOngoingCommand();
            model.unassignOngoingChangeForPerson(target.getId());
        }
        // personDataSnapshot == null means that the command was cancelled before any input was received
        final String targetName = personDataSnapshot == null ? "" : personDataSnapshot.fullName();
        final String targetIdString = personDataSnapshot == null ? "" : personDataSnapshot.idString();

        // catches CANCELLED and SUCCESS
        eventRaiser.accept(new SingleTargetCommandResultEvent(getCommandId(), COMMAND_TYPE, getState(),
                TARGET_TYPE, targetIdString, targetName, targetName));
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
            target = model.addViewablePersonWithoutBacking(input);
            target.setOngoingCommandType(OngoingCommandType.ADDING);
        });

        model.assignOngoingChangeToPerson(target.getId(), this);
        snapshotPersonData(target);
    }

    @Override
    protected void handleEditRequest(Supplier<Optional<ReadOnlyPerson>> editInputSupplier) {
        final Optional<ReadOnlyPerson> editInput = editInputSupplier.get();
        if (editInput.isPresent()) { // edit request confirmed
            cancelCommand();
            model.execNewAddPersonCommand(() -> editInput); // spawn new add request with the updated info
        }
    }

    @Override
    protected void handleDeleteRequest() {
        // do nothing, let it get cancelled
        cancelCommand();
    }

    @Override
    protected void handleResolveConflict() {
        // No conflicts possible
    }

    @Override
    protected void handleRetry() {
        cancelCommand();
        model.execNewAddPersonCommand(() -> Optional.of(input));
    }

    @Override
    protected Optional<ReadOnlyPerson> getRemoteConflictData() {
        return Optional.empty(); // no possible conflict for add command
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
        assert input != null;

        final CompletableFuture<ReadOnlyPerson> responseHolder = new CompletableFuture<>();
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
        if (target != null) {
            PlatformExecUtil.runAndWait(() -> model.visibleModel().removePerson(target.getId()));
        }
    }

    @Override
    protected void finishWithSuccess() {
        PlatformExecUtil.runAndWait(() -> {
            model.addPersonToBackingModelSilently(backingFromRemote); // so it wont trigger creation of another VP
            model.unassignOngoingChangeForPerson(getTargetPersonId()); // removes mapping for old id
            target.connectBackingObject(backingFromRemote); // changes id to that of backing person
            model.assignOngoingChangeToPerson(target.getId(), this); // remap this change for the new id
        });
        snapshotPersonData(target); // update snapshot for remote assigned id
    }

    private void snapshotPersonData(ReadOnlyPerson data) {
        personDataSnapshot = new Person(data);
    }

}
