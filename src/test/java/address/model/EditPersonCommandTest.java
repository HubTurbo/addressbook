package address.model;

import address.events.sync.UpdatePersonOnRemoteRequestEvent;
import address.model.ChangeObjectInModelCommand.CommandState;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.person.ViewablePerson;
import address.util.Config;
import address.testutil.TestUtil;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EditPersonCommandTest {

    private static final String ADDRESSBOOK_NAME = "ADDRESSBOOK NAME";

    private static class InterruptAndTerminateException extends RuntimeException {}

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    ModelManager modelManagerMock;
    ModelManager modelManagerSpy;
    @Mock
    Config config;
    EventBus events;

    public static final int TEST_ID = 93;
    ViewablePerson testTarget;
    ReadOnlyPerson inputData = TestUtil.generateSamplePersonWithAllData(0);

    final Supplier<Optional<ReadOnlyPerson>> returnValidEmptyInput = () -> Optional.of(Person.createPersonDataContainer());

    private static Supplier<Optional<ReadOnlyPerson>> inputRetrieverWrapper(ReadOnlyPerson inputValue) {
        return () -> Optional.of(inputValue);
    }

    @BeforeClass
    public static void beforeSetup() throws TimeoutException {
        TestUtil.initRuntime();
    }

    @AfterClass
    public static void teardown() throws Exception {
        TestUtil.tearDownRuntime();
    }

    @Before
    public void setup() {
        testTarget = ViewablePerson.fromBacking(TestUtil.generateSamplePersonWithAllData(TEST_ID));
        when(config.getAddressBookName()).thenReturn(ADDRESSBOOK_NAME);
        modelManagerSpy = spy(new ModelManager(config));
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

        (new EditPersonCommand(0, testTarget, returnValidEmptyInput, 0, null, modelManagerSpy, ADDRESSBOOK_NAME)).run();

        verify(otherCommand).waitForCompletion();
        verify(modelManagerSpy, never()).assignOngoingChangeToPerson(any(), any());
    }

    @Test
    public void getTargetPersonId_returnsCorrectId() {
        final EditPersonCommand epc = new EditPersonCommand(0, testTarget, null, 0, null, modelManagerMock, ADDRESSBOOK_NAME);
        assertEquals(epc.getTargetPersonId(), TEST_ID);
    }

    @Test
    public void retrievingInput_cancelsCommand_whenEmptyInputOptionalRetrieved() {
        final EditPersonCommand epc = new EditPersonCommand(0, testTarget, Optional::empty, 0,  e -> {}, modelManagerMock, ADDRESSBOOK_NAME);
        epc.run();
        assertEquals(epc.getState(), CommandState.CANCELLED);
    }

    @Test
    public void optimisticUiUpdate_simulatesCorrectData() {
        final EditPersonCommand epc = spy(new EditPersonCommand(0, testTarget, inputRetrieverWrapper(inputData),
                0, null, modelManagerSpy, ADDRESSBOOK_NAME));

        doThrow(new InterruptAndTerminateException()).when(epc).afterState(CommandState.SIMULATING_RESULT);
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
                0, events::post, modelManagerSpy, ADDRESSBOOK_NAME);
        epc.run();
        assertTrue(epc.getViewable().dataFieldsEqual(inputData));
        assertTrue(epc.getViewable().getBacking().dataFieldsEqual(inputData));
    }

}
