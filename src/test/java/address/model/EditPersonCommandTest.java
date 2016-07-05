package address.model;

import address.events.UpdatePersonOnRemoteRequestEvent;
import address.model.ChangeObjectInModelCommand.State;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.person.ViewablePerson;
import address.util.JavafxRuntimeUtil;
import address.util.TestUtil;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EditPersonCommandTest {

    private static class InterruptAndTerminateException extends RuntimeException {}

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    ModelManager modelManagerMock;
    ModelManager modelManagerSpy;
    @Mock
    UserPrefs prefs;
    EventBus events;

    public static final int TEST_ID = 93;
    ViewablePerson testTarget;
    ReadOnlyPerson inputData = TestUtil.generateSamplePersonWithAllData(0);

    final Supplier<Optional<ReadOnlyPerson>> returnValidEmptyInput = () -> Optional.of(Person.createPersonDataContainer());

    private static Supplier<Optional<ReadOnlyPerson>> inputRetrieverWrapper(ReadOnlyPerson inputValue) {
        return () -> Optional.of(inputValue);
    }

    @BeforeClass
    public static void beforeSetup() {
        JavafxRuntimeUtil.initRuntime();
    }

    @AfterClass
    public static void teardown() throws Exception {
        JavafxRuntimeUtil.tearDownRuntime();
    }

    @Before
    public void setup() {
        testTarget = ViewablePerson.fromBacking(TestUtil.generateSamplePersonWithAllData(TEST_ID));
        modelManagerSpy = spy(new ModelManager(prefs));
        when(prefs.getSaveFileName()).thenReturn("ADDRESSBOOK NAME");
        when(modelManagerMock.getPrefs()).thenReturn(prefs);
        events = new EventBus();
        events.register(new Object() {
            @Subscribe
            public void fakeAddToRemote(UpdatePersonOnRemoteRequestEvent e) {
                e.getReturnedPersonContainer().complete(e.getUpdatedPerson());
            }
        });
    }

    @Test
    public void waitsForOtherOngoingCommandsOnTargetToFinish() throws InterruptedException {
        final AddPersonCommand otherCommand = mock(AddPersonCommand.class);
        when(modelManagerSpy.personHasOngoingChange(TEST_ID)).thenReturn(true);
        when(modelManagerSpy.getOngoingChangeForPerson(TEST_ID)).thenReturn(otherCommand);

        doThrow(InterruptAndTerminateException.class).when(otherCommand).waitForCompletion(); // don't actually wait
        thrown.expect(InterruptAndTerminateException.class);

        (new EditPersonCommand(0, testTarget, returnValidEmptyInput, 0, null, modelManagerSpy)).run();

        verify(otherCommand).waitForCompletion();
        verify(modelManagerSpy, never()).assignOngoingChangeToPerson(any(), any());
    }

    @Test
    public void getTargetPersonId_returnsCorrectId() {
        final EditPersonCommand epc = new EditPersonCommand(0, testTarget, null, 0, null, modelManagerMock);
        assertEquals(epc.getTargetPersonId(), TEST_ID);
    }

    @Test
    public void retrievingInput_cancelsCommand_whenEmptyInputOptionalRetrieved() {
        final EditPersonCommand epc = new EditPersonCommand(0, testTarget, Optional::empty, 0, null, modelManagerMock);
        epc.run();
        assertEquals(epc.getState(), State.CANCELLED);
    }

    @Test
    public void optimisticUiUpdate_simulatesCorrectData() {
        final EditPersonCommand epc = spy(new EditPersonCommand(0, testTarget, inputRetrieverWrapper(inputData),
                0, null, modelManagerSpy));

        // to stop the run at start of grace period (right after simulated change)
        doThrow(new InterruptAndTerminateException()).when(epc).beforeGracePeriod();
        thrown.expect(InterruptAndTerminateException.class);

        epc.run();

        assertTrue(epc.getViewable().dataFieldsEqual(inputData)); // same data as input
        assertEquals(modelManagerSpy.visibleModel().getPersonList().size(), 1); // only 1 viewable
        assertTrue(modelManagerSpy.backingModel().getPersonList().isEmpty()); // simulation wont affect backing
        assertSame(modelManagerSpy.visibleModel().getPersonList().get(0), epc.getViewable()); // same ref
    }

    @Test
    public void succesfulEdit_updatesBackingModelCorrectly() {
        final EditPersonCommand epc = new EditPersonCommand(0, testTarget, inputRetrieverWrapper(inputData),
                0, events::post, modelManagerSpy);
        epc.run();
        assertTrue(epc.getViewable().dataFieldsEqual(inputData));
        assertTrue(epc.getViewable().getBacking().dataFieldsEqual(inputData));
    }

    // THIS TEST TAKES >=1 SECONDS BY DESIGN
    @Test
    public void interruptGracePeriod_withEditRequest_changesEditResult() {
        // grace period duration must be non zero, will be interrupted immediately anyway
        final EditPersonCommand epc = spy(new EditPersonCommand(0, testTarget, returnValidEmptyInput, 1, events::post, modelManagerSpy));
        final Supplier<Optional<ReadOnlyPerson>> editInputWrapper = inputRetrieverWrapper(inputData);

        doNothing().when(epc).beforeGracePeriod(); // don't wipe interrupt code injection when grace period starts
        epc.editInGracePeriod(editInputWrapper); // pre-specify apc will be interrupted by edit
        epc.run();

        assertTrue(epc.getViewable().dataFieldsEqual(inputData));
        assertTrue(epc.getViewable().getBacking().dataFieldsEqual(inputData));
    }

    @Test
    public void interruptGracePeriod_withDeleteRequest_cancelsAndSpawnsDeleteCommand() {
        // grace period duration must be non zero, will be interrupted immediately anyway
        final EditPersonCommand epc = spy(new EditPersonCommand(0, testTarget, returnValidEmptyInput, 1, null, modelManagerSpy));

        doNothing().when(modelManagerSpy).execNewDeletePersonCommand(any());
        doNothing().when(epc).beforeGracePeriod(); // don't wipe interrupt code injection when grace period starts
        epc.deleteInGracePeriod(); // pre-specify epc will be interrupted by delete

        epc.run();

        verify(modelManagerSpy).execNewDeletePersonCommand(testTarget);
        assertEquals(epc.getState(), State.CANCELLED);
    }

    @Test
    public void interruptGracePeriod_withCancelRequest_undoesSimulation() {
        final ReadOnlyPerson targetSnapshot = new Person(testTarget);
        // grace period duration must be non zero, will be interrupted immediately anyway
        final EditPersonCommand epc = spy(new EditPersonCommand(0, testTarget, returnValidEmptyInput, 1, null, modelManagerSpy));

        doNothing().when(epc).beforeGracePeriod(); // don't wipe interrupt code injection when grace period starts
        epc.cancelInGracePeriod(); // pre-specify epc will be interrupted by cancel

        epc.run();

        assertTrue(epc.getViewable().dataFieldsEqual(targetSnapshot));
        assertEquals(epc.getState(), State.CANCELLED);
    }
}
