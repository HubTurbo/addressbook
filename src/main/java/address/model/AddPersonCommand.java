package address.model;

import static address.model.ChangeObjectInModelCommand.State.*;
import address.events.EventManager;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.person.ViewablePerson;
import address.util.PlatformExecUtil;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Handles optimistic UI updating and consistency logic for adding a person to the addressbook.
 */
public class AddPersonCommand extends ChangePersonInModelCommand {

    private final EventManager eventManager;
    private final ModelManager model;
    private ViewablePerson targetViewable;

    /**
     * @param inputRetriever Will run on execution {@link #run()} thread. This should handle thread concurrency
     *                       logic (eg. {@link PlatformExecUtil#callLater(Callable)} within itself.
     *                       If the returned Optional is empty, the command will be cancelled.
     * @see super#ChangePersonInModelCommand(Supplier, int)
     */
    protected AddPersonCommand(Supplier<Optional<ReadOnlyPerson>> inputRetriever, int gracePeriodDurationInSeconds,
                               EventManager eventManager, ModelManager model) {
        super(inputRetriever, gracePeriodDurationInSeconds);
        this.model = model;
        this.eventManager = eventManager;
    }

    @Override
    public int getTargetPersonId() {
        if (targetViewable == null) {
            throw new IllegalStateException("Add person command has not created the proposed ViewablePerson yet.");
        }
        return targetViewable.getId();
    }

    @Override
    protected void before() {

    }

    @Override
    protected void after() {
        if (targetViewable != null) {
            // free target viewable for other commands
            final ChangePersonInModelCommand removed = model.unassignOngoingChangeForPerson(targetViewable.getId());
        }
    }

    /**
     * Creates the viewableperson and adds it to the visible person list in model.
     * It will have no backing person at first (backing person is connected only after remote confirmation)
     */
    @Override
    protected State simulateResult() {
        // create VP and add to model
        targetViewable = ViewablePerson.withoutBacking(new Person(input));
        PlatformExecUtil.runAndWait(() -> {
            model.addViewablePerson(targetViewable);
            model.assignOngoingChangeToPerson(targetViewable.getId(), this);
        });
        return GRACE_PERIOD;
    }

    @Override
    protected void beforeGracePeriod() {
        // nothing needed for now
    }

    @Override
    protected void handleChangeToSecondsLeftInGracePeriod(int secondsLeft) {
        assert targetViewable != null;
        PlatformExecUtil.runLater(() -> targetViewable.setSecondsLeftInPendingState(secondsLeft));
    }

    @Override
    protected State handleEditInGracePeriod(Supplier<Optional<ReadOnlyPerson>> editInputSupplier) {
        // take details and update viewable, then restart grace period
        final Optional<ReadOnlyPerson> editInput = editInputSupplier.get();
        if (editInput.isPresent()) { // update proposed new person's details
            input = editInput.get();
            PlatformExecUtil.runAndWait(() -> targetViewable.simulateUpdate(input));
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
        // TODO: update when remote request api is complete
        model.unassignOngoingChangeForPerson(getTargetPersonId()); // removes mapping for old id
        PlatformExecUtil.runAndWait(() -> {
            final Person backingPerson = new Person(model.generatePersonId()).update(targetViewable);
            model.addPersonToBackingModelSilently(backingPerson); // so it wont trigger creation of another VP
            targetViewable.connectBackingObject(backingPerson); // changes id to that of backing person
        });
        model.assignOngoingChangeToPerson(getTargetPersonId(), this); // remap this change for the new id
        return SUCCESSFUL;
    }

    @Override
    protected void finishWithCancel() {
        if (targetViewable != null) {
            PlatformExecUtil.runAndWait(() -> model.visibleModel().removePerson(targetViewable.getId()));
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
