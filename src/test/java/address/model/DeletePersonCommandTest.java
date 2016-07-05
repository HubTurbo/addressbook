package address.model;

import address.events.DeletePersonOnRemoteRequestEvent;
import address.model.ChangeObjectInModelCommand.State;
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
public class DeletePersonCommandTest {

    private static class InterruptAndTerminateException extends RuntimeException {}

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    ModelManager modelManagerMock;
    ModelManager modelManagerSpy;
    @Mock
    UserPrefs prefs;
    EventBus events;

    public static final int TEST_ID = 314;
    ViewablePerson testTarget;

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
        testTarget = spy(ViewablePerson.fromBacking(TestUtil.generateSamplePersonWithAllData(TEST_ID)));
        modelManagerSpy = spy(new ModelManager(prefs));
        when(prefs.getSaveFileName()).thenReturn("ADDRESSBOOK NAME");
        when(modelManagerMock.getPrefs()).thenReturn(prefs);
        events = new EventBus();
        events.register(new Object() {
            @Subscribe
            public void fakeDeleteOnRemote(DeletePersonOnRemoteRequestEvent e) {
                e.getResultContainer().complete(true);
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

        (new DeletePersonCommand(0, testTarget, 0, null, modelManagerSpy)).run();

        verify(otherCommand).waitForCompletion();
        verify(modelManagerSpy, never()).assignOngoingChangeToPerson(any(), any());
    }

    @Test
    public void getTargetPersonId_returnsCorrectId() {
        final DeletePersonCommand epc = new DeletePersonCommand(0, testTarget, 0, null, modelManagerMock);
        assertEquals(epc.getTargetPersonId(), TEST_ID);
    }

    @Test
    public void optimisticUiUpdate_flagsDelete() {
        final DeletePersonCommand dpc = spy(new DeletePersonCommand(0, testTarget, 0, events::post, modelManagerSpy));

        // to stop the run at start of grace period (right after simulated change)
        doThrow(new InterruptAndTerminateException()).when(dpc).beforeGracePeriod();
        thrown.expect(InterruptAndTerminateException.class);

        dpc.run();
        verify(testTarget).setIsDeleted(true);
    }

    @Test
    public void succesfulDelete_updatesBackingModelCorrectly() {
        final DeletePersonCommand dpc = new DeletePersonCommand(0, testTarget, 0, events::post, modelManagerSpy);

        modelManagerSpy.visibleModel().addPerson(testTarget);
        modelManagerSpy.addPersonToBackingModelSilently(testTarget.getBacking());
        dpc.run();

        assertTrue(modelManagerSpy.backingModel().getPersons().isEmpty());
        assertTrue(modelManagerSpy.visibleModel().getPersons().isEmpty());
    }

    @Test
    public void interruptGracePeriod_withEditRequest_cancelsAndSpawnsEditCommand() {
        // grace period duration must be non zero, will be interrupted immediately anyway
        final DeletePersonCommand dpc = spy(new DeletePersonCommand(0, testTarget, 1, null, modelManagerSpy));
        final Supplier<Optional<ReadOnlyPerson>> editInputRetriever = Optional::empty;

        doNothing().when(modelManagerSpy).execNewEditPersonCommand(any(), any());
        doNothing().when(dpc).beforeGracePeriod(); // don't wipe interrupt code injection when grace period starts
        dpc.editInGracePeriod(editInputRetriever); // pre-specify dpc will be interrupted by delete

        dpc.run();

        verify(modelManagerSpy).execNewEditPersonCommand(testTarget, editInputRetriever);
        assertEquals(dpc.getState(), State.CANCELLED);
    }

    @Test
    public void interruptGracePeriod_withCancelRequest_undoesSimulation() {
        // grace period duration must be non zero, will be interrupted immediately anyway
        final DeletePersonCommand dpc = spy(new DeletePersonCommand(0, testTarget, 1, null, modelManagerSpy));
        final Supplier<Optional<ReadOnlyPerson>> editInputRetriever = Optional::empty;

        modelManagerSpy.visibleModel().addPerson(testTarget);
        modelManagerSpy.addPersonToBackingModelSilently(testTarget.getBacking());

        doNothing().when(dpc).beforeGracePeriod(); // don't wipe interrupt code injection when grace period starts
        dpc.cancelInGracePeriod(); // pre-specify dpc will be interrupted by cancel

        dpc.run();

        assertEquals(modelManagerSpy.backingModel().getPersonList().size(), 1);
        assertEquals(modelManagerSpy.visibleModel().getPersonList().size(), 1);
        assertSame(modelManagerSpy.visibleModel().getPersonList().get(0), testTarget);
        assertSame(modelManagerSpy.backingModel().getPersonList().get(0), testTarget.getBacking());
        assertEquals(testTarget.isDeleted(), false);
        assertEquals(dpc.getState(), State.CANCELLED);
    }
}
