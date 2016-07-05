package address.model;

import static address.model.ChangeObjectInModelCommand.State.*;
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

    private static final AppLogger logger = LoggerManager.getLogger(AddPersonCommand.class);

    private final Consumer<BaseEvent> eventRaiser;
    private final ModelManager model;
    private ViewablePerson viewableToAdd;
    private final String addressbookName;

    /**
     * @param inputRetriever Will run on execution {@link #run()} thread. This should handle thread concurrency
     *                       logic (eg. {@link PlatformExecUtil#call(Callable)} within itself.
     *                       If the returned Optional is empty, the command will be cancelled.
     * @see super#ChangePersonInModelCommand(Supplier, int)
     */
    public AddPersonCommand(Supplier<Optional<ReadOnlyPerson>> inputRetriever, int gracePeriodDurationInSeconds,
                               Consumer<BaseEvent> eventRaiser, ModelManager model) {
        super(inputRetriever, gracePeriodDurationInSeconds);
        this.model = model;
        this.eventRaiser = eventRaiser;
        this.addressbookName = model.getPrefs().getSaveFileName();
    }

    protected ViewablePerson getViewableToAdd() {
        return viewableToAdd;
    }

    @Override
    public String getName() {
        return "Add Person " + (viewableToAdd == null ? "" : viewableToAdd.idString());
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
            model.unassignOngoingChangeForPerson(viewableToAdd.getId());
        }
        model.trackFinishedCommand(this);
    }

    /**
     * Creates the viewableperson and adds it to the visible person list in model.
     * It will have no backing person at first (backing person is connected only after remote confirmation)
     */
    @Override
    protected State simulateResult() {
        assert input != null;
        // create VP and add to model
        viewableToAdd = PlatformExecUtil.callAndWait(() -> model.addViewablePersonWithoutBacking(input), null);
        logger.debug("simulateResult: Going to add " + viewableToAdd.toString());
        model.assignOngoingChangeToPerson(viewableToAdd.getId(), this);
        logger.debug("simulateResult: Added " + viewableToAdd.toString() + " to visible person list in model");
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
            logger.debug("requestChangeToRemote -> id of viewable person before updating is " + backingPerson.getId());

            logger.debug("requestChangeToRemote: Removing mapping for old id:" + getTargetPersonId());
            model.unassignOngoingChangeForPerson(getTargetPersonId()); // removes mapping for old id

            PlatformExecUtil.runAndWait(() -> {
                model.addPersonToBackingModelSilently(backingPerson); // so it wont trigger creation of another VP
                viewableToAdd.connectBackingObject(backingPerson); // changes id to that of backing person
                model.assignOngoingChangeToPerson(backingPerson.getId(), this); // remap this change for the new id
            });
            logger.debug("requestChangeToRemote -> id of viewable person updated to " + viewableToAdd.getId());
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

}
