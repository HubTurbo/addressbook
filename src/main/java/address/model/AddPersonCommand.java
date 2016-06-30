package address.model;

import static address.model.ChangeObjectInModelCommand.State.*;
import address.events.BaseEvent;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.person.ViewablePerson;
import address.util.PlatformExecUtil;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Handles optimistic UI updating, cancellation/changing command, and remote consistency logic
 * for adding a person to the addressbook.
 */
public class AddPersonCommand extends ChangePersonInModelCommand {

    private final Consumer<? extends BaseEvent> eventRaiser;
    private final ModelManager model;
    private ViewablePerson viewableToAdd;

    /**
     * @param inputRetriever Will run on execution {@link #run()} thread. This should handle thread concurrency
     *                       logic (eg. {@link PlatformExecUtil#call(Callable)} within itself.
     *                       If the returned Optional is empty, the command will be cancelled.
     * @see super#ChangePersonInModelCommand(Supplier, int)
     */
    protected AddPersonCommand(Supplier<Optional<ReadOnlyPerson>> inputRetriever, int gracePeriodDurationInSeconds,
                               Consumer<? extends BaseEvent> eventRaiser, ModelManager model) {
        super(inputRetriever, gracePeriodDurationInSeconds);
        this.model = model;
        this.eventRaiser = eventRaiser;
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
    }

    /**
     * Creates the viewableperson and adds it to the visible person list in model.
     * It will have no backing person at first (backing person is connected only after remote confirmation)
     */
    @Override
    protected State simulateResult() {
        assert input != null;
        // create VP and add to model
        viewableToAdd = ViewablePerson.withoutBacking(new Person(input));
        PlatformExecUtil.runAndWait(() -> {
            model.addViewablePerson(viewableToAdd);
            model.assignOngoingChangeToPerson(viewableToAdd.getId(), this);
        });
        return GRACE_PERIOD;
    }

    @Override
    protected void beforeGracePeriod() {
        // nothing needed for now
    }

    @Override
    protected void handleChangeToSecondsLeftInGracePeriod(int secondsLeft) {
        assert viewableToAdd != null;
        PlatformExecUtil.runLater(() -> viewableToAdd.setSecondsLeftInPendingState(secondsLeft));
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
    protected State handleCancelInGracePeriod() {
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
        // TODO: update when remote request api is complete
        model.unassignOngoingChangeForPerson(getTargetPersonId()); // removes mapping for old id
        PlatformExecUtil.runAndWait(() -> {
            final Person backingPerson = new Person(model.generatePersonId()).update(viewableToAdd);
            model.addPersonToBackingModelSilently(backingPerson); // so it wont trigger creation of another VP
            viewableToAdd.connectBackingObject(backingPerson); // changes id to that of backing person
        });
        model.assignOngoingChangeToPerson(getTargetPersonId(), this); // remap this change for the new id
        return SUCCESSFUL;
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
        // no way to fail for now
    }

}
