package address.model;

import address.events.BaseEvent;
import address.model.ChangeObjectInModelCommand.State;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.person.ViewablePerson;
import address.util.PlatformExecUtil;
import address.util.TestUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Optional;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PlatformExecUtil.class)
@PowerMockIgnore("javax.*")
public class AddPersonCommandTest {

    public static class InterruptAndTerminateException extends RuntimeException {}

    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Mock
    ModelManager modelManagerMock;
    ModelManager modelManagerSpy;

    @Before
    public void setup() {
        // run all platformexecutil calls directly on current thread
        PowerMockito.spy(PlatformExecUtil.class);
        when(PlatformExecUtil.isFxThread()).thenReturn(true);

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
        final Supplier<Optional<ReadOnlyPerson>> returnValidInput = () -> Optional.of(Person.createPersonDataContainer());
        final ViewablePerson createdViewable = ViewablePerson.withoutBacking(Person.createPersonDataContainer());
        final int CORRECT_ID = createdViewable.getId();
        final AddPersonCommand apc = spy(new AddPersonCommand(returnValidInput, 0, null, modelManagerMock));

        when(modelManagerMock.addViewablePersonWithoutBacking(notNull(ReadOnlyPerson.class))).thenReturn(createdViewable);

        // to stop the run at start of grace period (right after simulated change)
        doThrow(new InterruptAndTerminateException()).when(apc).beforeGracePeriod();
        thrown.expect(InterruptAndTerminateException.class);

        apc.run();
        assertEquals(apc.getTargetPersonId(), CORRECT_ID);
    }

    @Test
    public void getTargetPersonId_returnsFinalId_AfterSuccess() {
        final Supplier<Optional<ReadOnlyPerson>> returnValidInput = () -> Optional.of(Person.createPersonDataContainer());
        final AddPersonCommand apc = new AddPersonCommand(returnValidInput, 0, null, modelManagerSpy);
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
        final AddPersonCommand apc = spy(new AddPersonCommand(() -> Optional.of(inputData), 0, null, modelManagerSpy));

        // to stop the run at start of grace period (right after simulated change)
        doThrow(new InterruptAndTerminateException()).when(apc).beforeGracePeriod();
        thrown.expect(InterruptAndTerminateException.class);

        apc.run();
        assertTrue(apc.getViewableToAdd().dataFieldsEqual(inputData));
    }

    private AddPersonCommand constructWithDummyArgs() {
        return new AddPersonCommand(null, 0, null, null);
    }
}
