package address.model;

import address.model.ChangeObjectInModelCommand.State;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.person.ViewablePerson;
import address.util.JavafxThreadingRule;
import address.util.TestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AddPersonCommandTest {

    private static class InterruptAndTerminateException extends RuntimeException {}

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Rule
    public JavafxThreadingRule javafxRule = new JavafxThreadingRule();

    @Mock
    ModelManager modelManagerMock;
    ModelManager modelManagerSpy;

    final Supplier<Optional<ReadOnlyPerson>> returnValidEmptyInput = () -> Optional.of(Person.createPersonDataContainer());

    private static Supplier<Optional<ReadOnlyPerson>> inputRetrieverWrapper(ReadOnlyPerson inputValue) {
        return () -> Optional.of(inputValue);
    }

    @Before
    public void setup() {
        modelManagerSpy = spy(new ModelManager(null));
    }

    @Test
    public void getTargetPersonId_throws_whenViewableNotCreatedYet() {
        final AddPersonCommand apc = constructWithDummyArgs();
        thrown.expect(IllegalStateException.class);
        apc.getTargetPersonId();
    }

    @Test
    public void getTargetPersonId_returnsTempId_afterResultSimulatedAndBeforeRemoteChange() {
        final ViewablePerson createdViewable = ViewablePerson.withoutBacking(Person.createPersonDataContainer());
        final int CORRECT_ID = createdViewable.getId();
        final AddPersonCommand apc = spy(new AddPersonCommand(returnValidEmptyInput, 0, null, modelManagerMock));

        when(modelManagerMock.addViewablePersonWithoutBacking(notNull(ReadOnlyPerson.class))).thenReturn(createdViewable);

        // to stop the run at start of grace period (right after simulated change)
        doThrow(InterruptAndTerminateException.class).when(apc).beforeGracePeriod();
        thrown.expect(InterruptAndTerminateException.class);

        apc.run();
        assertEquals(apc.getTargetPersonId(), CORRECT_ID);
    }

    @Test
    public void getTargetPersonId_returnsFinalId_AfterSuccess() {
        final AddPersonCommand apc = new AddPersonCommand(returnValidEmptyInput, 0, null, modelManagerSpy);
        final int CORRECT_ID = 1;
        when(modelManagerSpy.generatePersonId()).thenReturn(CORRECT_ID);

        apc.run();
        assertEquals(apc.getTargetPersonId(), CORRECT_ID);

    }

    @Test
    public void retrievingInput_cancelsCommand_whenEmptyInputOptionalRetrieved() {
        final AddPersonCommand apc = new AddPersonCommand(Optional::empty, 0, null, null);
        apc.run();
        assertEquals(apc.getState(), State.CANCELLED);
    }

    @Test
    public void optimisticUiUpdate_simulatesCorrectData() {
        final ReadOnlyPerson inputData = TestUtil.generateSamplePersonWithAllData(0);
        final AddPersonCommand apc = spy(new AddPersonCommand(inputRetrieverWrapper(inputData), 0, null, modelManagerSpy));

        // to stop the run at start of grace period (right after simulated change)
        doThrow(new InterruptAndTerminateException()).when(apc).beforeGracePeriod();
        thrown.expect(InterruptAndTerminateException.class);

        apc.run();

        assertTrue(apc.getViewableToAdd().dataFieldsEqual(inputData)); // same data as input
        assertEquals(modelManagerSpy.visibleModel().getPersonList().size(), 1); // only 1 viewable
        assertTrue(modelManagerSpy.backingModel().getPersonList().isEmpty()); // simulation wont affect backing
        assertSame(modelManagerSpy.visibleModel().getPersonList().get(0), apc.getViewableToAdd()); // same ref
    }

    @Test
    public void successfulAdd_updatesBackingModelCorrectly() {
        final ReadOnlyPerson inputData = TestUtil.generateSamplePersonWithAllData(0);
        final AddPersonCommand apc = new AddPersonCommand(inputRetrieverWrapper(inputData), 0, null, modelManagerSpy);

        apc.run();
        assertFalse(modelManagerSpy.personHasOngoingChange(apc.getViewableToAdd()));
        assertFinalStatesCorrectForSuccessfulAdd(apc, modelManagerSpy, inputData);
    }

    // THIS TEST TAKES >=1 SECONDS BY DESIGN
    @Test
    public void interruptGracePeriod_withEditRequest_changesAddedPersonData() {
        // grace period duration must be non zero, will be interrupted immediately anyway
        final AddPersonCommand apc = spy(new AddPersonCommand(returnValidEmptyInput, 1, null, modelManagerSpy));
        final Supplier<Optional<ReadOnlyPerson>> editInputWrapper = inputRetrieverWrapper(TestUtil.generateSamplePersonWithAllData(1));

        doNothing().when(apc).beforeGracePeriod(); // don't wipe interrupt code injection when grace period starts
        apc.editInGracePeriod(editInputWrapper); // pre-specify apc will be interrupted by edit
        apc.run();

        assertFinalStatesCorrectForSuccessfulAdd(apc, modelManagerSpy, editInputWrapper.get().get());
    }

    @Test
    public void interruptGracePeriod_withDeleteRequest_cancelsCommand() {
        // grace period duration must be non zero, will be interrupted immediately anyway
        final AddPersonCommand apc = spy(new AddPersonCommand(returnValidEmptyInput, 1, null, modelManagerSpy));

        doNothing().when(apc).beforeGracePeriod(); // don't wipe interrupt code injection when grace period starts
        apc.deleteInGracePeriod(); // pre-specify apc will be interrupted by delete
        apc.run();

        assertTrue(modelManagerSpy.backingModel().getPersonList().isEmpty());
        assertTrue(modelManagerSpy.visibleModel().getPersonList().isEmpty());
        assertFalse(modelManagerSpy.personHasOngoingChange(apc.getViewableToAdd()));
        assertEquals(apc.getState(), State.CANCELLED);
    }

    @Test
    public void interruptGracePeriod_withCancelRequest_undoesSimulation() {
        // grace period duration must be non zero, will be interrupted immediately anyway
        final AddPersonCommand apc = spy(new AddPersonCommand(returnValidEmptyInput, 1, null, modelManagerSpy));

        doNothing().when(apc).beforeGracePeriod(); // don't wipe interrupt code injection when grace period starts
        apc.cancelInGracePeriod(); // pre-specify apc will be interrupted by cancel
        apc.run();

        assertTrue(modelManagerSpy.backingModel().getPersonList().isEmpty());
        assertTrue(modelManagerSpy.visibleModel().getPersonList().isEmpty());
        assertFalse(modelManagerSpy.personHasOngoingChange(apc.getViewableToAdd()));
        assertEquals(apc.getState(), State.CANCELLED);
    }

    private void assertFinalStatesCorrectForSuccessfulAdd(AddPersonCommand command, ModelManager model, ReadOnlyPerson resultData) {
        assertEquals(command.getState(), State.SUCCESSFUL);
        assertEquals(model.visibleModel().getPersonList().size(), 1); // only 1 viewable
        assertEquals(model.backingModel().getPersonList().size(), 1); // only 1 backing

        final ViewablePerson viewablePersonFromModel = model.visibleModel().getPersons().get(0);
        final Person backingPersonFromModel = model.backingModel().getPersons().get(0);
        assertSame(viewablePersonFromModel, command.getViewableToAdd()); // reference check
        assertSame(viewablePersonFromModel.getBacking(), backingPersonFromModel); // backing connected properly to visible
        assertTrue(viewablePersonFromModel.dataFieldsEqual(resultData));
        assertTrue(backingPersonFromModel.dataFieldsEqual(resultData));
    }

    public static AddPersonCommand constructWithDummyArgs() {
        return new AddPersonCommand(null, 0, null, null);
    }
}
